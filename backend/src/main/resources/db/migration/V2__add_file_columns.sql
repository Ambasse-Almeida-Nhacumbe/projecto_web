ALTER TABLE projetos
ADD COLUMN relatorio_path VARCHAR(255),
ADD COLUMN pitch_path VARCHAR(255),
ADD COLUMN ultima_atualizacao_relatorio TIMESTAMP,
ADD COLUMN ultima_atualizacao_pitch TIMESTAMP; 