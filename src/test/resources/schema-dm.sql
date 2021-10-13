select tc.column_id,
       tc.COLUMN_NAME column_name,
       tc.DATA_TYPE   data_type,
       tc.DATA_LENGTH max_length,
       tc.NULLABLE    nullable,
       cc.COMMENTS    column_comment,
       it.INFO2       extra
from user_col_comments cc
         inner join user_tab_cols tc
                    on (cc.table_name = tc.table_name and cc.column_name = tc.column_name)
         left JOIN (SELECT *
                    FROM syscolumns t
                    WHERE id = (SELECT object_id
                           FROM dba_objects t
                           WHERE t.owner = ?
                             AND object_type = 'TABLE'
                             AND t.object_name = ?)
                   ) it on (it.NAME = tc.COLUMN_NAME)
where cc.table_name = upper(?)