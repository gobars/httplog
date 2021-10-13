select column_name,
       column_comment,
       data_type,
       extra,
       is_nullable              nullable,
       character_maximum_length max_length,
       ordinal_position         column_id
from information_schema.columns
where table_schema = database()
  and table_name = ?