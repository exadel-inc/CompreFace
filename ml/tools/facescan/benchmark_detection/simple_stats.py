import attr


@attr.s(auto_attribs=True)
class SimpleStats:
    scanner_name: str
    total_boxes: int = 0
    total_missed_boxes: int = 0
    total_noses: int = 0
    total_missed_noses: int = 0

    def add(self, total_boxes, total_missed_boxes, total_noses, total_missed_noses):
        self.total_boxes += total_boxes
        self.total_missed_boxes += total_missed_boxes
        self.total_noses += total_noses
        self.total_missed_noses += total_missed_noses

    def __str__(self, infix=False):
        infix = f'[{infix}] ' if infix else ""
        return (f"{infix}"
                f"Undetected faces: {self.total_missed_noses}/{self.total_noses}, "
                f"False face detections: {self.total_missed_boxes}/{self.total_boxes}")
