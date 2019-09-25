template = {
    "swagger": "2.0",
    "info": {
        "description": "Service for face recognition. You load images of people that you know, and then can load image of a person and recognize it.",
        "version": "0.1-snapshot",
        "title": "Exadel Face Recognition Service"
    },
    "host": "msqv355.exadel.by:5001",
    "tags": [
        {
            "name": "Recognition"
        }
    ],
    "schemes": [
        "http"
    ],
    "definitions": {
        "RecognizeResponse": {
            "type": "object",
            "properties": {
                "prediction": {
                    "type": "string",
                    "example": "Albert Einstein"
                },
                "probability": {
                    "type": "number",
                    "format": "float",
                    "example": "0.95"
                }
            }
        },
        "GetAllNamesResponse": {
            "type": "array",
            "items": {
                "type": "string",
                "example":  "Albert Einstein"
            }
        }
    },
    "externalDocs": {
        "description": "Find out more about Exadel Face Recognition Service",
        "url": "https://confluence.exadel.com/display/KC/Exadel+Face+Recognition+Service"
    }
}
