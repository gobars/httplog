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
      from pg_class c,
           pg_attribute a
               left join pg_description d on d.objoid = a.attrelid and d.objsubid = a.attnum
      where c.oid = a.attrelid
        and c.relname = ?
        and a.attnum > 0) b
         left join pg_attrdef ad on b.attnum = ad.adnum and ad.adrelid = b.oid