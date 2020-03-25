class JSONEncodable:
    def to_json(self):
        if hasattr(self, 'dto'):
            return self.dto.to_json()
        return self.__dict__
