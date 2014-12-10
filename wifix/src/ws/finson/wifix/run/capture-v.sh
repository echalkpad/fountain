#!/bin/bash
# capture 1 or more data scans

# This function actually performs the data scanning.

scan() {
echo "+++ Scan" >> data-file.txt
echo "+++ Date" >> data-file.txt
date >> data-file.txt
date +%s >> data-file.txt
echo "---" >> data-file.txt
echo "+++ WiFi" >> data-file.txt
echo "$(${scan_command})" >> data-file.txt
echo "---" >> data-file.txt
echo "+++ Photo" >> data-file.txt
echo "$(imagesnap -d 'HD Webcam C525' img-${1}.jpg)" >> data-file.txt
echo "---" >> data-file.txt
echo "---" >> data-file.txt
}

#  The main program initializes and then loops as requested

rm -f data-file.txt

sys_name=$(uname)
if [ ${sys_name} == "Darwin" ]
then
	scan_command='airport -s'
elif [ ${sys_name} == "Linux" ]
then
	scan_command="sudo iwlist scan"
else
	echo "OS type not recognized: ${sys_name}"
	exit 1
fi
echo ${scan_command}
echo

if [ -z "$1" ]                           # Is parameter #1 zero length?
then
  scan_count=1
else
  scan_count=$1
fi
echo "Scan count: ${scan_count}"

for idx in $(seq ${scan_count})
do
echo "+++ $idx of ${scan_count}"
scan $idx
done

echo "Done."
archive_name="session-$(date +%s)"
mv data-file.txt ${archive_name}-raw.txt
echo "Data stored in file '${archive_name}-raw.txt'"
mkdir ${archive_name}-img.d
mv *.jpg ${archive_name}-img.d
echo "Images stored in directory '${archive_name}-img.d'"
