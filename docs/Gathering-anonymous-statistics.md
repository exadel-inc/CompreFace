# Gathering Anonymous Statistics

To better understand which features we should add to the service and
improve it further, we implemented functionality for gathering anonymous
statistics. This section aims to describe what exact information we
collect.

We respect the privacy of our users; this is why all statistics are
strictly anonymized before being sent to our servers. There is no
possibility to de-anonymize received information. In short, we collect
information about how many users, applications, services, and faces your
installation has. During the first user sign-up, there is a sign "Agree
to send anonymous statistics." By checking it, you agree with Exadel
Privacy Policy and agree to send anonymous statistics to our servers.

#### What we collect:

-   Event of user registration - we record only the fact of the creation
    of a new user. We do not gather any information about the user (like
    name, email password, etc.).
-   Event of application creation - we record only the fact of the
    creation of a new application. We do not gather any information
    about the application(like name, which users have access to it,
    etc.).
-   Event of service creation - we record only the creation of a new
    service and its type. We do not gather any information about the
    service(like name, etc.).
-   The number of saved faces in Face Recognition service Collection.
    Every day we record how many faces are saved in Collection in
    ranges: 1-10, 11-50, 51-200, 201-500, 501-2000, 2001-10000,
    10001-50000, 50001-200000, 200001-1000000, 1000001+. We do not
    gather any information about the faces(like face name, embedding,
    etc.).

#### What we do NOT collect:

-   Any personal information of CompreFace users or the end-users
-   Any names you use in CompreFace
-   Any information about hardware, software, or location of the host
    machine

During the first start, we assign to the CompreFace installation the
`install_guid` variable. This variable is random; there is no
possibility to retrieve any information from it; the only purpose of
this variable is to understand that gathered statistics were sent from
one machine. We send it in every request to our server to understand
that this is the same installation as before.

#### Examples of saved data:
```csv
"_createdAt:date","install_guid:string","action_name:string"
"2021-03-15 09:16:31.676","560eee90-5fca-11eb-988b-0242ac120003","USER_CREATE"
"2021-03-15 09:16:32.031","560eee90-5fca-11eb-988b-0242ac120003","APP_CREATE"
"2021-03-15 09:16:32.291","560eee90-5fca-11eb-988b-0242ac120003","FACE_DETECTION_CREATE"
"2021-03-15 09:16:32.607","560eee90-5fca-11eb-988b-0242ac120003","FACE_VERIFICATION_CREATE"
"2021-03-15 09:16:32.998","560eee90-5fca-11eb-988b-0242ac120003","FACE_RECOGNITION_CREATE"
```
```csv
"_createdAt:date","install_guid:string","collection_guid:string","faces_range:string"
"2021-03-13 13:25:49.700","59638de4-5fca-11eb-848b-0242ac120002","a3d5dda8-b53a-4465-a44e-f1c3c81c7551","501-2000"
"2021-03-13 13:25:49.840","59638de4-5fca-11eb-848b-0242ac120002","a4594ccc-198a-492e-8146-8bbf27972296","0"
"2021-03-13 13:25:50.003","59638de4-5fca-11eb-848b-0242ac120002","39c1925d-a1a9-4d44-8eb3-6acf132b89f2","1-10"
"2021-03-13 13:25:50.763","59638de4-5fca-11eb-848b-0242ac120002","794dd0ec-ac88-4552-90a8-f0bb0ddcee1e","201-500"
```
#### How we use the data

The data is used to understand the popularity of different services, how
many faces usually are saved to face collection and how many users use
CompreFace on an ongoing basis. We do not provide this data to third
parties in any case. However, we still can publish aggregated data in
self-promotional goals, like "CompreFace has N active users" or
"CompreFace is successfully used with face collections that stores more
than 1 million faces".

If you have any questions about the privacy policy, what data we
collect, or how we use it, please [get in touch with
us](mailto:compreface.support@exadel.com)
