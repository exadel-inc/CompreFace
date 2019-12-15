"""
Usage:
python -m pytest test_e2e.py [--host <HOST:PORT>] [--drop-db]

Arguments:
    --host <HOST:PORT>      Run E2E against this host, default value: http://localhost:5001
    --drop-db               Drop and reinitialize database before E2E test

Instructions:
1. Start the Face Recognition Service
2. Run command, for example
python -m pytest test_e2e.py --host http://localhost:5001
"""

import os
from pathlib import Path

import pytest
import requests

from main import ROOT_DIR

CURRENT_DIR = Path(os.path.dirname(os.path.realpath(__file__)))
IMG_DIR = ROOT_DIR / 'test_files'
TRAINING_TIMEOUT_S = 60




@pytest.fixture
def host(request):
    return request.config.getoption('host')


def after_previous_gen():
    order_no = 1
    while True:
        yield order_no
        order_no += 1


after_previous = after_previous_gen()



@pytest.mark.run(order=next(after_previous))
def test__when_client_checks_service_availability__returns_200(host):
    pass

    res = requests.get(f"{host}/status")

    assert res.status_code == 200, res.content
    assert res.json()['status'] == 'OK'


@pytest.mark.run(order=next(after_previous))
def test__when_client_opens_apidocs__returns_200(host):
    pass

    res = requests.get(f"{host}/apidocs")

    assert res.status_code == 200, res.status_code


@pytest.mark.run(order=next(after_previous))
def test__when_client_tries_to_scan_an_image_without_faces__then_returns_400_no_face_found(host):
    files = {'file': open(IMG_DIR / 'landscape.jpg', 'rb')}

    res = requests.post(f"{host}/scan_faces", files=files)

    assert res.status_code == 400, res.content
    assert res.json()['message'] == "No face is found in the given image"


@pytest.mark.run(order=next(after_previous))
def test__when_client_requests_to_scan_face__then_correct_box_and_embedding_returned(host):

    files = {'file': open(IMG_DIR / 'e2e-personA-img1.jpg', 'rb')}

    res = requests.post(f"{host}/scan_faces", files=files)

    assert res.status_code == 200, res.content
    calc_version = res.json()['calculator_version']
    assert calc_version == "embedding_calc_model_20170512.pb"
    result = res.json()['result']
    assert result == [{'box': {'probability': 0.9997376799583435,
          'x_max': 284,
          'x_min': 146,
          'y_max': 373,
          'y_min': 193},
  'embedding': [-0.02487223595380783,
                -0.004412464797496796,
                0.06423000246286392,
                -0.028427142649888992,
                -0.13745813071727753,
                -0.010940118692815304,
                -0.10244692862033844,
                -0.04723384976387024,
                -0.01053684763610363,
                0.06020912900567055,
                -0.06675398349761963,
                0.004987603519111872,
                -0.14121297001838684,
                -0.0549207478761673,
                -0.09452006965875626,
                0.08220848441123962,
                0.15814293920993805,
                0.017958873882889748,
                0.041807740926742554,
                -0.11222463101148605,
                -0.03670116886496544,
                -0.03875758871436119,
                0.10583554208278656,
                -0.08434882760047913,
                0.10639218240976334,
                -0.022672271355986595,
                -0.024453990161418915,
                0.14440380036830902,
                -0.031111976131796837,
                0.026857072487473488,
                -0.032146356999874115,
                -0.11898966133594513,
                0.06752783805131912,
                0.03766043484210968,
                0.13994355499744415,
                0.06477198749780655,
                0.010054273530840874,
                0.09760849177837372,
                -0.019756030291318893,
                0.052115704864263535,
                -0.1882687509059906,
                -0.09231098741292953,
                -0.1474946141242981,
                -0.08826075494289398,
                0.19444940984249115,
                -0.04624415934085846,
                -0.11811001598834991,
                0.23376838862895966,
                -0.003183127148076892,
                -0.014892486855387688,
                -0.046951401978731155,
                -0.026299364864826202,
                0.11207381635904312,
                0.04227733984589577,
                0.004427047446370125,
                0.07661104202270508,
                0.02764512039721012,
                -0.04661092907190323,
                -0.012334289029240608,
                -0.03855672851204872,
                -0.03728446364402771,
                0.06775417923927307,
                -0.12562763690948486,
                -0.011157417669892311,
                -0.021181849762797356,
                0.1160174012184143,
                0.05719784274697304,
                0.01528775505721569,
                -0.0016730611678212881,
                0.00956361647695303,
                0.11410554498434067,
                -0.036524467170238495,
                0.03191220387816429,
                0.06085081398487091,
                0.022176675498485565,
                0.045903317630290985,
                0.03433484211564064,
                -0.018208211287856102,
                0.156014084815979,
                -0.05681696906685829,
                0.010735413059592247,
                -0.0429142601788044,
                -0.09556563943624496,
                -0.057535622268915176,
                0.19893673062324524,
                -0.05418750271201134,
                0.00913215521723032,
                -0.07211626321077347,
                0.03133255988359451,
                0.1683814823627472,
                -0.09665527194738388,
                0.10407492518424988,
                0.08028290420770645,
                0.08987626433372498,
                -0.05881248041987419,
                0.06953994184732437,
                -0.04819551110267639,
                0.20620547235012054,
                -0.11923292279243469,
                0.0410497710108757,
                0.20334506034851074,
                -0.020974954590201378,
                0.020426521077752113,
                -0.11327865719795227,
                -0.06113401800394058,
                0.15395566821098328,
                -0.11754999309778214,
                -0.030847948044538498,
                -0.0646408423781395,
                -0.05835849419236183,
                -0.06429639458656311,
                0.15304350852966309,
                -0.09492119401693344,
                -0.06873024255037308,
                -0.0027530004736036062,
                0.07524009793996811,
                -0.007441653870046139,
                -0.07030195742845535,
                -0.05925745144486427,
                0.056586578488349915,
                0.15539968013763428,
                -0.10950390249490738,
                -0.10949410498142242,
                -0.00885262805968523,
                0.135752871632576,
                0.08364396542310715,
                0.16874828934669495,
                -0.03604152798652649]}]

