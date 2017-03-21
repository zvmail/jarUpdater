#!/usr/bin/env python
# coding=utf-8
# vim:ts=4:sw=4:et

import os
import platform
import subprocess
import time

file_path = os.path.abspath(os.path.dirname(__file__)).replace('\\', '/')
updatesite_path = file_path + '/../'

cur_os = platform.system().lower()
if 'windows' == cur_os:
    md5exe = file_path + '/md5sum.exe'
else:
    md5exe = 'md5sum'

version_txt = updatesite_path + 'version.txt'

os.chdir(updatesite_path)

def interact_run(cmd):
    p = subprocess.Popen(cmd, 0, None, None, subprocess.PIPE, subprocess.PIPE, shell=True)
    (sout, serr) = p.communicate()

    ret_val = p.returncode
    ret_info = sout + serr

    return (ret_val, ret_info)

def jar_handle(per_jar):
    global new_version_txt

    (ret_val, ret_info) = interact_run('"%s" "%s"' % (md5exe, per_jar))
    if 0 != ret_val:
        print 'wrong: %s' % ret_info
        return
    else:
        new_version_txt = new_version_txt + '%s=%s%s' % (per_jar, ret_info.split(' ')[0].strip(), os.linesep)

if __name__ == '__main__':
    new_version_txt = 'version=%s%s' % (time.strftime("%Y-%m-%d-%H-%M-%S", time.localtime(time.time())), os.linesep)
    for (_, _, file) in os.walk(updatesite_path):
        file = [per_file for per_file in file if per_file.endswith('.jar')]
        map(jar_handle, file)
        break

    with open(version_txt, 'w') as f:
        f.write(new_version_txt)

    print 'done.'
