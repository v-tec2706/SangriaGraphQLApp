create table "Continent"
(
    "id"   BIGINT  NOT NULL PRIMARY KEY,
    "name" VARCHAR NOT NULL,
    "url"  VARCHAR NOT NULL
)
create table "Country"
(
    "id"          BIGINT  NOT NULL PRIMARY KEY,
    "continentId" BIGINT  NOT NULL,
    "name"        VARCHAR NOT NULL,
    "url"         VARCHAR NOT NULL
)
alter table "Country"
    add constraint "continentFk" foreign key ("continentId") references "Continent" ("id") on update RESTRICT on delete CASCADE
create table "City"
(
    "id"        BIGINT  NOT NULL PRIMARY KEY,
    "countryId" BIGINT  NOT NULL,
    "name"      VARCHAR NOT NULL,
    "url"       VARCHAR NOT NULL
)
alter table "City"
    add constraint "countryFk" foreign key ("countryId") references "Country" ("id") on update RESTRICT on delete CASCADE
create table "Company"
(
    "id"        BIGINT  NOT NULL PRIMARY KEY,
    "countryId" BIGINT  NOT NULL,
    "name"      VARCHAR NOT NULL,
    "url"       VARCHAR NOT NULL
)
alter table "Company"
    add constraint "countryFk" foreign key ("countryId") references "Country" ("id") on update RESTRICT on delete CASCADE
create table "Person"
(
    "id"           BIGINT  NOT NULL PRIMARY KEY,
    "cityId"       BIGINT  NOT NULL,
    "firsName"     VARCHAR NOT NULL,
    "lastsName"    VARCHAR NOT NULL,
    "gender"       VARCHAR NOT NULL,
    "birthday"     DATE    NOT NULL,
    "browserUsed"  VARCHAR NOT NULL,
    "creationDate" DATE    NOT NULL,
    "email"        text[]  NOT NULL,
    "speaks"       text[]  NOT NULL,
    "locationIP"   VARCHAR NOT NULL
)
alter table "Person"
    add constraint "cityFk" foreign key ("cityId") references "City" ("id") on update RESTRICT on delete CASCADE
create table "Forum"
(
    "id"           BIGINT  NOT NULL PRIMARY KEY,
    "moderatorId"  BIGINT  NOT NULL,
    "title"        VARCHAR NOT NULL,
    "creationDate" DATE    NOT NULL
)
alter table "Forum"
    add constraint "moderatorId" foreign key ("moderatorId") references "Person" ("id") on update RESTRICT on delete CASCADE
create table "Message"
(
    "id"           BIGINT  NOT NULL PRIMARY KEY,
    "countryId"    BIGINT  NOT NULL,
    "personId"     BIGINT  NOT NULL,
    "browserUsed"  VARCHAR NOT NULL,
    "creationDate" DATE    NOT NULL,
    "locationIP"   VARCHAR NOT NULL,
    "content"      VARCHAR NOT NULL,
    "length"       INTEGER NOT NULL
)
alter table "Message"
    add constraint "countryFk" foreign key ("countryId") references "Country" ("id") on update RESTRICT on delete CASCADE
alter table "Message"
    add constraint "personFk" foreign key ("personId") references "Person" ("id") on update RESTRICT on delete CASCADE
create table "Post"
(
    "forumId"   BIGINT NOT NULL,
    "language"  text[] NOT NULL,
    "imageFile" text[] NOT NULL
)
alter table "Post"
    add constraint "forumFk" foreign key ("forumId") references "Forum" ("id") on update RESTRICT on delete CASCADE
create table "TagClass"
(
    "id"   BIGINT  NOT NULL PRIMARY KEY,
    "name" VARCHAR NOT NULL,
    "url"  VARCHAR NOT NULL
)
create table "Topic"
(
    "id"   BIGINT  NOT NULL PRIMARY KEY,
    "name" VARCHAR NOT NULL,
    "url"  VARCHAR NOT NULL
)
create table "University"
(
    "id"     BIGINT  NOT NULL PRIMARY KEY,
    "cityId" BIGINT  NOT NULL,
    "name"   VARCHAR NOT NULL,
    "url"    VARCHAR NOT NULL
)
alter table "University"
    add constraint "cityFk" foreign key ("cityId") references "Country" ("id") on update RESTRICT on delete CASCADE
create table "ForumTagRelation"
(
    "forumId" BIGINT NOT NULL,
    "tagId"   BIGINT NOT NULL
)
alter table "ForumTagRelation"
    add constraint "forumFk" foreign key ("forumId") references "Forum" ("id") on update RESTRICT on delete CASCADE
alter table "ForumTagRelation"
    add constraint "tagFk" foreign key ("tagId") references "TagClass" ("id") on update RESTRICT on delete CASCADE
create table "HasMemberRelation"
(
    "forumId"  BIGINT NOT NULL,
    "personId" BIGINT NOT NULL
)
alter table "HasMemberRelation"
    add constraint "forumFk" foreign key ("forumId") references "Forum" ("id") on update RESTRICT on delete CASCADE
alter table "HasMemberRelation"
    add constraint "personFk" foreign key ("personId") references "Person" ("id") on update RESTRICT on delete CASCADE
create table "KnowsRelation"
(
    "personId" BIGINT NOT NULL,
    "friendId" BIGINT NOT NULL
)
alter table "KnowsRelation"
    add constraint "friendFk" foreign key ("personId") references "Person" ("id") on update RESTRICT on delete CASCADE
alter table "KnowsRelation"
    add constraint "personFk" foreign key ("personId") references "Person" ("id") on update RESTRICT on delete CASCADE
create table "LikesRelation"
(
    "personId"  BIGINT NOT NULL,
    "messageId" BIGINT NOT NULL
)
alter table "LikesRelation"
    add constraint "messageFk" foreign key ("messageId") references "Message" ("id") on update RESTRICT on delete CASCADE
alter table "LikesRelation"
    add constraint "personFk" foreign key ("personId") references "Person" ("id") on update RESTRICT on delete CASCADE
create table "MessageTagRelation"
(
    "messageId" BIGINT NOT NULL,
    "tagId"     BIGINT NOT NULL
)
alter table "MessageTagRelation"
    add constraint "messageFk" foreign key ("messageId") references "Message" ("id") on update RESTRICT on delete CASCADE
alter table "MessageTagRelation"
    add constraint "tagFk" foreign key ("tagId") references "TagClass" ("id") on update RESTRICT on delete CASCADE
create table "StudyAtRelation"
(
    "personId"     BIGINT NOT NULL,
    "universityId" BIGINT NOT NULL
)
alter table "StudyAtRelation"
    add constraint "personFk" foreign key ("personId") references "Person" ("id") on update RESTRICT on delete CASCADE
alter table "StudyAtRelation"
    add constraint "universityFk" foreign key ("universityId") references "University" ("id") on update RESTRICT on delete CASCADE
create table "WorkAtRelation"
(
    "personId"  BIGINT NOT NULL,
    "companyId" BIGINT NOT NULL
)
alter table "WorkAtRelation"
    add constraint "companyFk" foreign key ("companyId") references "Company" ("id") on update RESTRICT on delete CASCADE
alter table "WorkAtRelation"
    add constraint "personFk" foreign key ("personId") references "Person" ("id") on update RESTRICT on delete CASCADE