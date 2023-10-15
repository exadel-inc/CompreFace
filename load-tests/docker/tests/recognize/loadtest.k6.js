import sql from "k6/x/sql";
import http from "k6/http";
import { check } from 'k6';
import { FormData } from './formdata.js';

const PREDEFINED_IMAGES = [
    "./faces/FACE_1.jpg"
]

const XAPIKEY = '1f348698-6985-4296-914e-a5e3270cfad7'
const IMAGE_PATHS = __ENV.IMAGES ? __ENV.IMAGES.split(';') : PREDEFINED_IMAGES
const IMAGES = getImageFiles(IMAGE_PATHS)
const REQUEST_TIMEOUT = 360000
const db_init_sql = open("./db_init.sql")
const db_truncate_sql = open("./db_truncate.sql")
const db = sql.open("postgres", __ENV.DB_CONNECTION_STRING)


export let options = {
    scenarios: {
        my_awesome_api_test: {
            executor: 'constant-vus',
            vus: 8,
            duration: '1m', // possible opts "Xs" (X seconds), "Xm" (X minutes), "Xh" (X hours), "Xd" (X days)
        },
    },
    thresholds: {
        http_req_duration: ['p(99)<3000'], // 99% of requests must complete below 3s
    },
};

export function setup() {
    console.log("DB: " + __ENV.DB_CONNECTION_STRING)
    console.log("Host: " + __ENV.HOSTNAME)

    execute_sql(db_init_sql)
    return {}
}

export function teardown(data) {
    execute_sql(db_truncate_sql)
    db.close()
}

export default function(data) {
    let response = verify(IMAGES[IMAGE_PATHS[0]], IMAGES[IMAGE_PATHS[1]])
    check(response, {
        'status 200': (r) => r.status === 200,
        'probability': (r) => r.body.indexOf('probability') !== -1,
    })
}

function verify(image_file) {
    let url = __ENV.HOSTNAME + '/api/v1/recognition/recognize'

    const fd = new FormData();
    fd.append('file', http.file(image_file, 'file.jpg', 'image/jpeg'));
    fd.append('limit', '0');
    fd.append('prediction_count', '1');

    let headers = {
        'Content-Type': 'multipart/form-data; boundary=' + fd.boundary,
        'x-api-key': XAPIKEY,
    }

    let params = {headers: headers, timeout: REQUEST_TIMEOUT}

    return http.post(url, fd.body(), params)
}

function getImageFiles(image_paths) {
    let image_files = {}

    for (let index = 0; index < image_paths.length; ++index) {
        image_files[IMAGE_PATHS[index]] = open(image_paths[index], 'b')
    }

    return image_files
}

export function execute_sql(sql_string) {
    db.exec(sql_string)
}
