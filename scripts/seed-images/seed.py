"""
Seed sample histopathology images into the images-service.

Reads all image files from the samples/ directory next to this script,
parses metadata from filenames, and uploads each one via the REST API.
Skips images that are already uploaded (idempotent by title).
"""

import json
import mimetypes
import os
import re
import sys
import time
import logging
import requests

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger(__name__)

USER_SERVICE_URL  = os.environ["USER_SERVICE_URL"].rstrip("/")
IMAGES_SERVICE_URL = os.environ["IMAGES_SERVICE_URL"].rstrip("/")
SEED_EMAIL        = os.environ["SEED_EMAIL"]
SEED_PASSWORD     = os.environ["SEED_PASSWORD"]

SAMPLES_DIR = os.path.join(os.path.dirname(__file__), "samples")
IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".tiff", ".tif", ".svs", ".ndpi", ".scn"}

STAINS = ["HE", "H&E", "IHC", "PAS", "MASSON", "TRICHROME", "GIEMSA", "GOMORI", "AFB"]
TISSUES = [
    "kidney", "lung", "liver", "brain", "colon", "prostate", "breast",
    "skin", "bone", "spleen", "thyroid", "pancreas", "stomach", "bladder",
    "ovary", "testis", "lymph", "heart", "muscle", "nerve"
]
MAGNIFICATIONS = ["2.5x", "5x", "10x", "20x", "40x", "100x"]
KEYWORDS = ["tumor", "normal", "cancer", "carcinoma", "adenocarcinoma", "metastasis",
            "necrosis", "fibrosis", "inflammation", "cyst", "hyperplasia"]


def wait_for_service(url: str, name: str, retries: int = 20, delay: int = 5):
    health = f"{url}/actuator/health"
    for attempt in range(1, retries + 1):
        try:
            r = requests.get(health, timeout=5)
            if r.status_code == 200 and r.json().get("status") == "UP":
                log.info("%s is ready.", name)
                return
        except requests.RequestException:
            pass
        log.info("Waiting for %s... (%d/%d)", name, attempt, retries)
        time.sleep(delay)
    log.error("%s did not become ready in time. Exiting.", name)
    sys.exit(1)


def login() -> str:
    r = requests.post(
        f"{USER_SERVICE_URL}/api/auth/login",
        json={"username": SEED_EMAIL, "password": SEED_PASSWORD},
        timeout=10,
    )
    r.raise_for_status()
    token = r.json()["user"]["token"]
    log.info("Logged in as %s.", SEED_EMAIL)
    return token


def existing_titles(token: str) -> set:
    r = requests.get(
        f"{IMAGES_SERVICE_URL}/api/images/search",
        headers={"Authorization": f"Bearer {token}"},
        timeout=10,
    )
    r.raise_for_status()
    return {img["title"] for img in r.json().get("images", [])}


def parse_filename(name: str) -> dict:
    """Extract title, tags, and description from a histopathology filename."""
    stem = os.path.splitext(name)[0]
    readable = re.sub(r"[-_]+", " ", stem).strip()

    upper = stem.upper()
    tags = []

    for stain in STAINS:
        if stain.replace("&", "").replace(" ", "") in upper.replace("&", "").replace(" ", ""):
            tags.append(stain)

    for tissue in TISSUES:
        if tissue.upper() in upper:
            tags.append(tissue)

    for mag in MAGNIFICATIONS:
        if mag.upper().replace("X", "X") in upper:
            tags.append(mag)

    for kw in KEYWORDS:
        if kw.upper() in upper:
            tags.append(kw)

    # Build description from detected metadata
    parts = []
    detected_stain   = next((t for t in tags if t in STAINS), None)
    detected_tissue  = next((t for t in tags if t in TISSUES), None)
    detected_mag     = next((t for t in tags if t in MAGNIFICATIONS), None)
    detected_keyword = next((t for t in tags if t in KEYWORDS), None)

    if detected_tissue:
        parts.append(detected_tissue.capitalize() + " tissue")
    if detected_stain:
        parts.append(f"stained with {detected_stain}")
    if detected_mag:
        parts.append(f"imaged at {detected_mag} magnification")
    if detected_keyword:
        parts.append(f"showing {detected_keyword}")

    description = (", ".join(parts) + ".").capitalize() if parts else f"Histopathology sample: {readable}."

    return {
        "title": readable.title(),
        "tags": tags,
        "description": description,
    }


def upload(token: str, filepath: str, meta: dict):
    filename = os.path.basename(filepath)
    tags_str = json.dumps(meta["tags"])

    data = {
        "fileName": filename,
        "title": meta["title"],
        "description": meta["description"],
    }
    if tags_str:
        data["tagsList"] = tags_str

    content_type, _ = mimetypes.guess_type(filename)
    content_type = content_type or "application/octet-stream"

    log.info("Uploading '%s' | content_type=%s | tags=%s", filename, content_type, tags_str or "(none)")

    with open(filepath, "rb") as f:
        r = requests.post(
            f"{IMAGES_SERVICE_URL}/api/images",
            headers={"Authorization": f"Bearer {token}"},
            data=data,
            files={"imageFile": (filename, f, content_type)},
            timeout=60,
        )

    if not r.ok:
        log.error("Upload failed %s: %s — %s", r.status_code, filename, r.text)
        r.raise_for_status()

    log.info("Uploaded '%s' (id=%s)", meta["title"], r.json().get("id"))


def main():
    wait_for_service(USER_SERVICE_URL,   "user-service")
    wait_for_service(IMAGES_SERVICE_URL, "images-service")

    token  = login()
    exists = existing_titles(token)

    if not os.path.isdir(SAMPLES_DIR):
        log.error("samples/ directory not found at %s", SAMPLES_DIR)
        sys.exit(1)

    files = [
        f for f in os.listdir(SAMPLES_DIR)
        if os.path.splitext(f)[1].lower() in IMAGE_EXTENSIONS
    ]

    if not files:
        log.warning("No image files found in samples/. Nothing to seed.")
        return

    seeded = 0
    for filename in sorted(files):
        meta = parse_filename(filename)
        if meta["title"] in exists:
            log.info("Skipping '%s' — already uploaded.", meta["title"])
            continue
        upload(token, os.path.join(SAMPLES_DIR, filename), meta)
        seeded += 1

    log.info("Done. %d image(s) seeded.", seeded)


if __name__ == "__main__":
    main()
