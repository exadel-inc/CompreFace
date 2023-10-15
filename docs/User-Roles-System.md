# User Roles System

CompreFace roles system consists of two types of roles - global roles
and application roles. The users with these roles have different
responsibilities, so we recommend that you delimit such users and follow
our recommendations to avoid giving too much access to sensitive
information. Of course, in small teams and at your own risk, you can
ignore these recommendations.

## Global Roles

Global roles define what permissions you have in the system itself, and
the primary responsibility of such users is to maintain the system
itself. Therefore, we recommend adding the most permissive (owner and
administrator) roles to technical support employees. Then there is no
reason to add such users to applications as they still have all
permissions within the application.

In CompreFace, the first user automatically receives the global owner
role and has rights for any operation within CompreFace - managing users
and creating and managing applications. The only restriction for the
global owner is that such a user can't delete themselves from the
system, so the user has to assign the global owner role to somebody else
and then remove themselves from the system.

Users with the global administrator role have the same permissions as
users with the global owner role. The only difference is that such users
can\'t manage the user with the global owner role. We recommend reducing
users with such a role to the minimum number required to maintain the
system.

All new users are automatically assigned the global user role. These
users can't create applications, can access only the applications they
were added to, and can't manage other users. These users use CompreFace
for face recognition and are part of the development team; they are not
responsible for managing other users and their permissions.

## Application Roles

Application roles define the user's role within an application, and the
primary responsibility of such users is to develop applications into
which they are going to integrate CompreFace. We recommend that the most
permissive roles (owner and administrator) were added as project
managers and team leads, as they are responsible for the application. We
also recommend that all application users have the global user role. To
become a member of an application team, users with a global user role
need to be added to the application directly by the global owner, global
administrator, or application owner.

The user that creates an application automatically receives the
application owner role and has rights for any operation within the
application - managing the application and its users and creating and
managing [Face Services](Face-services-and-plugins.md). The only
restriction for the application owner is that they can't delete
themselves from the application, so they have to assign the application
owner role to somebody else before deleting themselves.

Users with the application administrator role (global user role +
application administrator role) can create and manage [Face
Services](Face-services-and-plugins.md) but can't manage an application
and its users.

Users with the application user role can't manage anything in the
application. This is the least permissive role (global user role +
application user role), but this provides enough information to
integrate CompreFace with any other application, so we recommend that
most CompreFace users have this role.
