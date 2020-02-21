
    -- postgres specific indexes
    create index ix_ebasic_jmjb_gin2 on ebasic_json_map_json_b using gin(content jsonb_path_ops);
  