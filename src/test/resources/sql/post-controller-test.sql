INSERT INTO member (id, email, password, username)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'test@example.com', 'qwer1234', 'test');

INSERT INTO member_roles (member_id, role)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ROLE_USER');

INSERT INTO folder (id, member_id, parent_id, depth, order_index, title)
VALUES ('00000000-0000-0000-0000-000000000000', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NULL, 0, 0, 'test folder');

INSERT INTO post (id, member_id, title, content, folder_id, is_deleted, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'test title', 'test content',
        '00000000-0000-0000-0000-000000000000', false, NOW() - INTERVAL '2' MINUTE, NOW()),
       ('11111111-1111-1111-1111-111111111112', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'test title2', 'test content',
        '00000000-0000-0000-0000-000000000000', false, NOW() - INTERVAL '1' MINUTE, NOW()),
       ('11111111-1111-1111-1111-111111111113', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '테스트 제목', 'test content',
        '00000000-0000-0000-0000-000000000000', false, NOW(), NOW());

INSERT INTO tag (id, name)
VALUES ('22222222-2222-2222-2222-222222222222', 'tag1');

INSERT INTO post_tag (post_id, tag_id)
VALUES ('11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222'),
       ('11111111-1111-1111-1111-111111111113', '22222222-2222-2222-2222-222222222222');