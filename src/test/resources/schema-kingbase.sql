select column_id,
       column_name,
       max_length,
       nullable,
       ad.adsrc extra,
       column_comment
from (select a.attnum      column_id,
             a.attname     column_name,
             a.atttypmod   max_length,
             a.attnotnull  nullable,
             c.oid         oid,
             a.attnum      attnum,
             d.description column_comment
      from sys_class c,
           sys_attribute a
               left join sys_description d on d.objoid = a.attrelid and d.objsubid = a.attnum
      where c.oid = a.attrelid
        and c.relname = ?
        and a.attnum > 0) b
         left join sys_attrdef ad on b.attnum = ad.adnum and ad.adrelid = b.oid