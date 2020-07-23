INSERT INTO public.user(id, email, password, first_name, last_name, account_non_expired, account_non_locked, credentials_non_expired, enabled, guid)
VALUES
(2147483647,'c509063d-3e1d-4277-9a72-d7b808c3635c@email.com','$2a$10$MrVO.yx9Ei1oKSTJr3NXm.4FWxNDbWRitjAZo3mEyCk72jCmon5RO','FirstName','LastName',true,true,true,true,'246cade7-1a82-4df1-aeb1-8fada9f4231c');

INSERT INTO organization(id, name, guid)
VALUES
 	(1000001, 'org_to_remove', 'd098a11e-c4e4-4f56-86b2-85ab3bc83044'),
	(1000002, 'other_org', 'ce3a22c6-348d-4066-95c8-831202f3e59c');


INSERT INTO app(id, name, guid, organization_id, api_key)
VALUES
	(2000001, 'app_to_remove1', 'b25db13e-cf4f-490b-808d-7495e5b95090', 1000001, '2ec2a23c-f19a-40d6-8a2f-835388023a51'),
	(2000002, 'app_to_remove2', '7abc5ba1-51fc-4802-9413-13c1ba628a7a', 1000001, 'e2cc86dc-020c-4f7d-9cec-3d09fc6b36ce'),
	(2000003, 'other_app', '700c8fae-0fa3-4185-9034-bbbd1bf70559', 1000002, 'd6492f09-17a2-4eaf-83e2-59763db503b3');


INSERT INTO model(id, name, guid, app_id, api_key)
VALUES
	(3000001, 'model_to_remove1', '525d51db-6d5d-4fb5-a8d0-9cd202fb83bc', 2000001, 'bc8e0ae6-522b-4965-add2-b6e713b10a9c'),
	(3000002, 'model_to_remove2', '9a4eb752-ccb6-420a-8a5a-ae21adbe0756', 2000001, 'd24c5558-b49e-411d-b322-6d423b9f22d7'),
	(3000003, 'shared_model_to_remove3', '6e3fe946-f963-4c9c-a3a6-bb1d193f275f', 2000002, '5de2940c-d9fe-4e60-845c-d8147dda9e94'),
	(3000004, 'shared_model', '6e79d5df-d416-45d6-abfc-767e05d4deb0', 2000003, '62189826-6a08-4d6e-abb7-e4979d6dc01c'),
	(3000005, 'other_model', 'd3a3fb0f-3930-469e-86eb-eed0fb34b56c', 2000003, 'e3323054-4c81-4b48-8028-449d5263ec8d');


INSERT INTO user_app_role(app_id, user_id, role)
VALUES
	(2000001, 2147483647, 'A'),
	(2000002, 2147483647, 'O'),
	(2000003, 2147483647, 'A');


INSERT INTO model_share_request(app_id, request_id, request_time)
VALUES
	(2000001, '22d7f072-cda0-4601-a95d-979fc37c67ce', now()),
	(2000002, '2f79f6c4-bff2-4206-8ada-0c19d38e55d9', now()),
	(2000003, '29dc3df0-af51-4d48-8a5c-bfc26032c853', now());


INSERT INTO app_model(app_id, model_id, access_type)
VALUES
	(2000001, 3000003, 'R'),
	(2000003, 3000003, 'R'),
	(2000001, 3000004, 'R');


INSERT INTO user_organization_role(organization_id, user_id, role)
VALUES
	(1000001, 2147483647, 'O' ),
	(1000002, 2147483647, 'O' );