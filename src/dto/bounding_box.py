import attr

from src.dto.serializable import Serializable


@attr.s(auto_attribs=True)
class BoundingBox(Serializable):
    xmin: int = attr.ib(converter=int)
    ymin: int = attr.ib(converter=int)
    xmax: int = attr.ib(converter=int)
    ymax: int = attr.ib(converter=int)
