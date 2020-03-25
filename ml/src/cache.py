"""
This file should contain all objects that are cached in-memory between requests
"""
from src.services.utils.pyutils import run_once


@run_once
def get_scanner():
    from src.services.facescan.scanner import Scanner
    return Scanner.Facenet2018()
