from typing import List, Tuple

import attr


@attr.s(auto_attribs=True, frozen=True)
class Row:
    img_name: str
    noses: List[Tuple[int, int]]
    include_to_tests: bool = True

    def __str__(self):
        return self.img_name


@attr.s(auto_attribs=True, frozen=True)
class Person:
    img_names: Tuple[str, ...]
    is_male: bool = None
    age: int = None

    def __iter__(self):
        return self.img_names.__iter__()


SAMPLE_IMAGES = [
    Row('000_5.jpg', [(219, 105), (304, 251), (392, 218), (469, 309), (600, 299)]),
    Row('001_A.jpg', [(2109, 2261)]),
    Row('002_A.jpg', [(2146, 2505)]),
    Row('003_A.jpg', [(3210, 1382)]),
    Row('004_A.jpg', [(1312, 1969)]),
    Row('005_A.jpg', [(2092, 2871)]),
    Row('006_A.jpg', [(1864, 3041)]),
    Row('007_B.jpg', [(205, 299)]),
    Row('008_B.jpg', [(225, 256)]),
    Row('009_C.jpg', [(166, 236)]),
    Row('010_2.jpg', [(348, 232), (506, 262)]),
    Row('011_3.jpg', [(314, 258), (449, 230), (385, 216)]),
    Row('012_4.jpg', [(214, 213), (314, 242), (438, 233), (625, 247)]),
    Row('013_4.jpg', [(275, 195), (335, 192), (423, 200), (492, 195)]),
    Row('014_5.jpg', [(98, 283), (207, 265), (405, 175), (602, 270), (687, 305)]),
    Row('015_6.jpg', [(161, 229), (267, 266), (350, 281), (450, 271), (555, 264), (635, 250)]),
    Row('016_8.jpg', [(197, 277), (262, 171), (261, 292), (355, 282), (437, 212), (452, 288), (517, 163),
                      (684, 202)]),
    Row('017_0.jpg', [])
]
name_2_annotation = {r.img_name: r.noses for r in SAMPLE_IMAGES}


PERSONS = (
    Person(img_names=('001_A.jpg', '002_A.jpg', '003_A.jpg', '004_A.jpg',
                      '005_A.jpg', '006_A.jpg'), is_male=False, age=30),
    Person(img_names=('007_B.jpg', '008_B.jpg'), is_male=False, age=25),
    Person(img_names=('009_C.jpg',), is_male=True, age=30),
)
name_2_person = {img_name: p for p in PERSONS for img_name in p.img_names}