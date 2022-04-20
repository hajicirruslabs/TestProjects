#!/bin/sh

set -eu

"$SEMMLE_DIST/tools/linux64/Semmle.Autobuild.CSharp" || exit $?
