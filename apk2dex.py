

# coding=utf-8

import os
import zipfile

path = "F:/apkSrc/100files"  # this is apk files' store path
dex_path = "F:/apkSrc/100files/dex/"  # a directory  store dex files

apklist = os.listdir(path)  # get all the names of apps

if not os.path.exists(dex_path):
    os.makedirs(dex_path)

for APK in apklist:
    portion = os.path.splitext(APK)

    if portion[1] == ".apk":
        newname = portion[0] + ".zip"  # change them into zip file to extract dex files
        
        os.chdir(path)
        os.rename(APK,newname)

    if APK.endswith(".zip"):
        apkname = portion[0]

        zip_apk_path = os.path.join(path, APK)  # get the zip files

        z = zipfile.ZipFile(zip_apk_path, 'r')  # read zip files

        for filename in z.namelist():
            if filename.endswith(".dex"):
                dexfilename = apkname + ".dex"
                dexfilepath = os.path.join(dex_path, dexfilename)
                f = open(dexfilepath, 'w+')  # eq: cp classes.dex dexfilepath
                f.write(z.read(filename))

print "all work done!"