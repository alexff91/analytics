CREATE TABLE simplecrud_db.address (ID INT, CITY VARCHAR(40), COUNTRY VARCHAR(40), STREET VARCHAR(40), SUBURB VARCHAR(40));
INSERT INTO simplecrud_db.address
(ID, CITY, COUNTRY, STREET, SUBURB)
VALUES
  (1, 'city', 'country', 'street', 'suburb'),
  (2, 'city', 'country', 'street', 'suburb'),
  (3, 'city', 'country', 'street', 'suburb'),
  (4, 'city', 'country', 'street', 'suburb'),
  (5, 'city', 'country', 'street', 'suburb'),
  (6, 'city', 'country', 'street', 'suburb'),
  (7, 'city', 'country', 'street', 'suburb'),
  (8, 'city', 'country', 'street', 'suburb'),
  (9, 'city', 'country', 'street', 'suburb'),
  (10, 'city', 'country', 'street', 'suburb');

CREATE TABLE simplecrud_db.role (ID INT, ROLEDESC VARCHAR(40), ROLENAME VARCHAR(40));
INSERT INTO simplecrud_db.role
(ID, ROLEDESC, ROLENAME)
VALUES
  (1, 'Administrator', 'Administrators'),
  (2, 'Manager', 'Managers'),
  (3, 'User', 'Users');

CREATE TABLE simplecrud_db.usertable (ID        INT, EMAIL VARCHAR(40),
                                      FIRSTNAME VARCHAR(40), LASTNAME VARCHAR(40), PASSWORD VARCHAR(256), USERNAME VARCHAR(40), ADDRESS_ID INT);


INSERT INTO simplecrud_db.usertable
(ID, EMAIL, FIRSTNAME, LASTNAME, PASSWORD, USERNAME, ADDRESS_ID)
VALUES
  (1, 'e-mail', 'name', 'surname',
   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'admin', 1),
  (2, 'e-mail', 'name', 'surname',
   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'user1', 2),
  (3, 'e-mail', 'name', 'surname',
   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'user2', 3),
  (4, 'e-mail', 'name', 'surname',
   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'user3', 4),
  (5, 'e-mail', 'name', 'surnamer',
   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'user4', 5),
  (6, 'e-mail', 'name', 'surname',
   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'user5', 6),
  (7, 'e-mail', 'name', 'surname',
   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'user6', 7),
  (8, 'e-mail', 'name', 'surname',
   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'user7', 8),
  (9, 'e-mail', 'name', 'surname',
   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'user8', 9),
  (10, 'e-mail', 'name', 'surname',
   '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'user9', 10);

CREATE TABLE simplecrud_db.user_roles (Role_roleid INT, User_userid INT);


INSERT INTO simplecrud_db.user_roles
(Role_roleid, User_userid)
VALUES
  (1, 1),
  (3, 2),
  (3, 3),
  (3, 4),
  (3, 5),
  (3, 6),
  (3, 7),
  (3, 8),
  (3, 9),
  (3, 10);


CREATE VIEW simplecrud_db.user_role_view
AS
  SELECT
    simplecrud_db.usertable.USERNAME
      AS username,
    simplecrud_db.usertable.PASSWORD
      AS password,
    simplecrud_db.role.ROLENAME
      AS rolename
  FROM ((simplecrud_db.user_roles
    JOIN simplecrud_db.usertable
      ON ((simplecrud_db.user_roles.User_userid = simplecrud_db.usertable.ID)))
    JOIN simplecrud_db.role ON ((simplecrud_db.user_roles.Role_roleid = simplecrud_db.role.ID)));

CREATE TABLE simplecrud_db.user_files (File_fileid INT, User_userid INT);

INSERT INTO simplecrud_db.user_files
(File_fileid, User_userid)
VALUES
  (1, 1),
  (1, 2),
  (1, 3),
  (2, 4),
  (1, 5),
  (1, 6),
  (1, 7),
  (1, 8),
  (2, 9),
  (1, 10);
CREATE TABLE simplecrud_db.file (id INT);
INSERT INTO simplecrud_db.file
(id)
VALUES
  (1),
  (2);

ALTER TABLE simplecrud_db.file
ADD file_name VARCHAR(400);