from json import JSONEncoder

import numpy as np

from src.services.dto.json_encodable import JSONEncodable


def add_json_encoding(app):
    class AppJSONEncoder(JSONEncoder):
        def default(self, obj):
            if isinstance(obj, JSONEncodable):
                return obj.to_json()
            if isinstance(obj, np.ndarray):
                return obj.tolist()
            return super().default(obj)

    app.json_encoder = AppJSONEncoder
