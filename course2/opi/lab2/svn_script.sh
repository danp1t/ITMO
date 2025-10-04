#!/bin/bash

cd ~/github/ITMO/opi/lab2
svnadmin create svn_repo

mkdir -p temp_svn/{trunk,branches,tags}
svn import temp_svn file://$(pwd)/svn_repo -m "Init repo structure"
rm -rf temp_svn

unzip -o commit0.zip -d temp_commit0
svn import temp_commit0 file://$(pwd)/svn_repo/trunk -m "Initial commit"
rm -rf temp_commit0

svn checkout file://$(pwd)/svn_repo/trunk svn_user_1
svn checkout file://$(pwd)/svn_repo/trunk svn_user_2


cd svn_user_2
svn copy file:///home/danp1t/github/ITMO/opi/lab2/svn_repo/trunk \
         file:///home/danp1t/github/ITMO/opi/lab2/svn_repo/branches/master \
         -m "Creating branch master"

svn switch file:///home/danp1t/github/ITMO/opi/lab2/svn_repo/branches/master


unzip -o ../commit1.zip
svn add --force .
svn commit -m "First commit"

svn copy file:///home/danp1t/github/ITMO/opi/lab2/svn_repo/trunk \
         file:///home/danp1t/github/ITMO/opi/lab2/svn_repo/branches/develop \
         -m "Creating branch develop"

svn switch file:///home/danp1t/github/ITMO/opi/lab2/svn_repo/branches/develop

unzip -o ../commit2.zip
svn add --force .
svn commit -m "Second commit"

unzip -o ./../commit3.zip
svn add --force .
svn commit -m "Third commit"

cd ../svn_user_1
unzip -o ./../commit4.zip
svn add --force .
svn commit -m "Fourth commit"

unzip -o ./../commit5.zip
svn add --force .
svn commit -m "Fifth commit"

cd ../svn_user_2
unzip -o ./../commit6.zip
svn add --force .
svn commit -m "Sixth commit"

cd ../svn_user_1
unzip -o ./../commit7.zip
svn add --force .
svn commit -m "Seventh commit"

cd ../svn_user_2
unzip -o ./../commit8.zip
svn add --force .
svn commit -m "Eigth commit"

cd ../svn_user_1
unzip -o ./../commit9.zip
svn add --force .
svn commit -m "Nine commit"
svn delete /home/danp1t/github/ITMO/opi/lab2/svn_user_1/\*
svn update
svn merge ^/branches/develop --accept mine-full
svn resolve --accept working -R .
svn commit -m "Merged trunk, resolved conflicts using 'mine-full' strategy"

cd ../svn_user_2
svn switch file:///home/danp1t/github/ITMO/opi/lab2/svn_repo/branches/master
unzip -o ./../commit10.zip
svn add --force .
svn commit -m "Ten commit"


cd ../svn_user_1
unzip -o ./../commit11.zip
svn update
svn add  .
svn commit -m "Eleven commit"
svn update
svn merge ^/branches/master --accept mine-full
svn resolve --accept working -R .
svn commit -m "Merged trunk, resolved conflicts using 'mine-full' strategy"

unzip -o ./../commit12.zip
svn add --force .
svn commit -m "Twelve commit"

unzip -o ./../commit13.zip
svn add --force .
svn commit -m "Thirteen commit"

unzip -o ./../commit14.zip
svn add --force .
svn commit -m "Fourteen commit"
