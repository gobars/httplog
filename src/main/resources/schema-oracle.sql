select tc.column_id,
       tc.COLUMN_NAME column_name,
       tc.DATA_TYPE   data_type,
       tc.DATA_LENGTH max_length,
       tc.NULLABLE    nullable,
       cc.COMMENTS    column_comment
from all_col_comments cc
         inner join all_tab_cols tc
                    on (cc.table_name = tc.table_name and cc.column_name = tc.column_name)
where cc.table_name = upper(?)