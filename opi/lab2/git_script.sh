#!/bin/bash

cd ~/github/ITMO/opi/lab2/
mkdir git_repo_user_1
cd git_repo_user_1/

git init
unzip -o ./../commit0.zip
git add .
git commit -m "Initial commit"

cd ..
git clone git_repo_user_1 git_repo_user_2
cd git_repo_user_2
unzip -o ./../commit1.zip
git add .
git commit -m "First commit"

git branch develop
git checkout develop
unzip -o ./../commit2.zip
git add .
git commit -m "Second commit"

unzip -o ./../commit3.zip
git add .
git commit -m "Third commit"

cd ../git_repo_user_1
unzip -o ./../commit4.zip
git add .
git commit -m "Fourth commit"

unzip -o ./../commit5.zip
git add .
git commit -m "Fifth commit"

cd ../git_repo_user_2
unzip -o ./../commit6.zip
git add .
git commit -m "Sixth commit"

cd ../git_repo_user_1
unzip -o ./../commit7.zip
git add .
git commit -m "Seventh commit"

cd ../git_repo_user_2
unzip -o ./../commit8.zip
git add .
git commit -m "Eigth commit"

cd ../git_repo_user_1
unzip -o ./../commit9.zip
git add .
git commit -m "Nine commit"
git remote add local-clone ./../git_repo_user_2
git fetch local-clone
git merge -X ours --no-commit --no-ff local-clone/develop
git add .
git commit -m "Merged git_repo_user_2/develop, resolved conflicts using 'ours' strategy"

cd ../git_repo_user_2
git checkout master
unzip -o ./../commit10.zip
git add .
git commit -m "ten commit"

cd ../git_repo_user_1
unzip -o ./../commit11.zip
git add .
git commit -m "Eleven commit"
git fetch local-clone
git merge -X ours --no-commit --no-ff local-clone/master
git add .
git commit -m "Merged git_repo_user_2/master, resolved conflicts using 'ours' strategy"

unzip -o ./../commit12.zip
git add .
git commit -m "Twelve commit"

unzip -o ./../commit13.zip
git add .
git commit -m "Thirteen commit"

unzip -o ./../commit14.zip
git add .
git commit -m "Fourteen commit"
