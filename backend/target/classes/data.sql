-- Limpar tabelas existentes para evitar conflitos
DELETE FROM mentorias;
DELETE FROM projetos;
DELETE FROM usuarios;

-- Inserir usuário admin padrão (senha: admin123)
INSERT INTO usuarios (nome, email, senha, role, data_criacao)
VALUES ('Administrador', 'admin@incubadora.com', '$2a$10$8.UnVuRZNEj5KOx3agnWSOj3gw9FkNk2HHFwna3jk26Gd.3HguMFu', 'ADMIN', CURRENT_TIMESTAMP);

-- Inserir mentor de exemplo (senha: mentor123)
INSERT INTO usuarios (nome, email, senha, role, data_criacao)
VALUES ('Mentor Exemplo', 'mentor@incubadora.com', '$2a$10$8.UnVuRZNEj5KOx3agnWSOj3gw9FkNk2HHFwna3jk26Gd.3HguMFu', 'MENTOR', CURRENT_TIMESTAMP);

-- Inserir usuário regular de exemplo (senha: user123)
INSERT INTO usuarios (nome, email, senha, role, data_criacao)
VALUES ('Usuário Exemplo', 'user@incubadora.com', '$2a$10$8.UnVuRZNEj5KOx3agnWSOj3gw9FkNk2HHFwna3jk26Gd.3HguMFu', 'USUARIO', CURRENT_TIMESTAMP);

-- Inserir projetos de exemplo
INSERT INTO projetos (nome, descricao, usuario_id, status, data_criacao, data_atualizacao)
SELECT 'Sistema de Gestão de Saúde', 
       'Sistema de gestão digital para clínicas e centros de saúde comunitários, incluindo agendamento de consultas, prontuário eletrônico e lembretes automatizados por SMS.',
       (SELECT id FROM usuarios WHERE email = 'user@incubadora.com'),
       'PENDENTE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP;

INSERT INTO projetos (nome, descricao, usuario_id, status, data_criacao, data_atualizacao)
SELECT 'Plataforma de Educação Online', 
       'Plataforma de ensino à distância com foco em cursos técnicos, incluindo sistema de videoaulas, exercícios interativos e certificação digital.',
       (SELECT id FROM usuarios WHERE email = 'user@incubadora.com'),
       'EM_ANALISE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP;

INSERT INTO projetos (nome, descricao, usuario_id, status, data_criacao, data_atualizacao)
SELECT 'App de Reciclagem Colaborativa', 
       'Aplicativo móvel para conectar pessoas e empresas de reciclagem, facilitando a coleta seletiva e promovendo a economia circular.',
       (SELECT id FROM usuarios WHERE email = 'mentor@incubadora.com'),
       'APROVADO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP; 