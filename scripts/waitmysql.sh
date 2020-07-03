#
echo Starting MySQL...

status="1"
while [ "$status" != "0" ]; do
  sleep 5
  mysql -h127.0.0.1 -uroot -proot -e 'select VERSION()'
  status=$?
done

mysql -h127.0.0.1 -uroot -proot -e 'select VERSION()'