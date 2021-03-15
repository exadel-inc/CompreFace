# Gathering Anonymous Statistics 

To better understand which features we should add to the service and how we can improve it further we implemented functionality for gathering anonymous statistics. This section aims to describe what exact information we collect.

We respect the privacy of our users, this is why all statistics are strictly anonymized before sent to our servers. There is no possibility to de-anonymize received information. In short, we collect information about how many users, applications, services, and faces your installation has. During the first user sign up there is a sign “Agree to send anonymous statistics”. By checking it you agree with Exadel Privacy Policy and agree to send anonymous statistics to our servers.

What we collect:
* Event of user registration - we record only the fact of the creation of a new user. We do not gather any information about the user
(like name, email password, etc.).
* Event of application creation - we record only the fact of the creation of a new application. We do not gather any information about the application(like name, which users have access to it, etc.).
* Event of service creation - we record only the fact of the creation of a new service and its type. We do not gather any information about 
  the service(like name, etc.).
* Number of saved faces in Face Recognition service Collection. Every day we record how many faces are saved in Collection in ranges: 1-10, 11-50, 51-200, 201-500, 501-2000, 2001-10000, 10001-50000, 50001-200000, 200001-1000000, 1000001+. We do not gather any information about the faces(like face name, embedding, etc.).

What we do NOT collect:
* Any personal information of CompreFace users or the end-users
* Any names you use in CompreFace
* Any information about hardware, software, or location of the host machine

During the first start, we assign to the CompreFace installation the `install_guid` variable. This variable is totally random, there is no possibility to retrieve any information from it, the only purpose of this variable is to understand that gathered statistics were sent from one machine. We send it in every request to our server to understand that this is the same installation as before.

If you have any questions about the privacy policy, what data we collect or how we use it, please [contact us](mailto:compreface.support@exadel.com)