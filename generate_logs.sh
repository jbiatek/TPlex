#!/bin/sh
echo Plan simple-drive, script single-drive
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/simple-drive.plx -s tests/edu/umn/crisys/plexil/test/resources/single-drive.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/simple-drive___single-drive.log
echo Plan simple-drive, script double-drive
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/simple-drive.plx -s tests/edu/umn/crisys/plexil/test/resources/double-drive.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/simple-drive___double-drive.log
echo Plan Increment-test, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/Increment-test.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/Increment-test___empty.log
echo Plan Increment-test2, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/Increment-test2.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/Increment-test2___empty.log
echo Plan CruiseControl, script CruiseControl
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/CruiseControl.plx -s tests/edu/umn/crisys/plexil/test/resources/CruiseControl.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/CruiseControl___CruiseControl.log
echo Plan DriveToSchool, script DriveToSchool
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/DriveToSchool.plx -s tests/edu/umn/crisys/plexil/test/resources/DriveToSchool.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/DriveToSchool___DriveToSchool.log
echo Plan DriveToTarget, script DriveToTarget
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/DriveToTarget.plx -s tests/edu/umn/crisys/plexil/test/resources/DriveToTarget.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/DriveToTarget___DriveToTarget.log
echo Plan SafeDrive, script SafeDrive
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/SafeDrive.plx -s tests/edu/umn/crisys/plexil/test/resources/SafeDrive.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/SafeDrive___SafeDrive.log
echo Plan SimpleDrive, script SimpleDrive
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/SimpleDrive.plx -s tests/edu/umn/crisys/plexil/test/resources/SimpleDrive.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/SimpleDrive___SimpleDrive.log
echo Plan array1, script array1
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/array1.plx -s tests/edu/umn/crisys/plexil/test/resources/array1.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/array1___array1.log
echo Plan array3, script array3
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/array3.plx -s tests/edu/umn/crisys/plexil/test/resources/array3.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/array3___array3.log
echo Plan array4, script array4
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/array4.plx -s tests/edu/umn/crisys/plexil/test/resources/array4.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/array4___array4.log
echo Plan array8, script array8
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/array8.plx -s tests/edu/umn/crisys/plexil/test/resources/array8.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/array8___array8.log
echo Plan command1, script command1
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/command1.plx -s tests/edu/umn/crisys/plexil/test/resources/command1.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/command1___command1.log
echo Plan command2, script command2
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/command2.plx -s tests/edu/umn/crisys/plexil/test/resources/command2.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/command2___command2.log
echo Plan command3, script command3
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/command3.plx -s tests/edu/umn/crisys/plexil/test/resources/command3.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/command3___command3.log
echo Plan command4, script command4
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/command4.plx -s tests/edu/umn/crisys/plexil/test/resources/command4.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/command4___command4.log
echo Plan command5, script command5
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/command5.plx -s tests/edu/umn/crisys/plexil/test/resources/command5.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/command5___command5.log
echo Plan long_command, script long_command
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/long_command.plx -s tests/edu/umn/crisys/plexil/test/resources/long_command.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/long_command___long_command.log
echo Plan uncle_command, script uncle_command
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/uncle_command.plx -s tests/edu/umn/crisys/plexil/test/resources/uncle_command.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/uncle_command___uncle_command.log
echo Plan repeat2, script repeat2
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/repeat2.plx -s tests/edu/umn/crisys/plexil/test/resources/repeat2.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/repeat2___repeat2.log
echo Plan repeat5, script repeat5
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/repeat5.plx -s tests/edu/umn/crisys/plexil/test/resources/repeat5.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/repeat5___repeat5.log
echo Plan repeat7, script repeat7
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/repeat7.plx -s tests/edu/umn/crisys/plexil/test/resources/repeat7.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/repeat7___repeat7.log
echo Plan repeat8, script repeat8
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/repeat8.plx -s tests/edu/umn/crisys/plexil/test/resources/repeat8.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/repeat8___repeat8.log
echo Plan atomic-assignment, script atomic-assignment
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/atomic-assignment.plx -s tests/edu/umn/crisys/plexil/test/resources/atomic-assignment.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/atomic-assignment___atomic-assignment.log
echo Plan boolean1, script boolean1
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/boolean1.plx -s tests/edu/umn/crisys/plexil/test/resources/boolean1.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/boolean1___boolean1.log
echo Plan change-lookup-test, script change-lookup-test
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/change-lookup-test.plx -s tests/edu/umn/crisys/plexil/test/resources/change-lookup-test.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/change-lookup-test___change-lookup-test.log
echo Plan AncestorReferenceTest, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/AncestorReferenceTest.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/AncestorReferenceTest___empty.log
echo Plan assign-failure-with-conflict, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/assign-failure-with-conflict.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/assign-failure-with-conflict___empty.log
echo Plan assign-to-parent-exit, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/assign-to-parent-exit.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/assign-to-parent-exit___empty.log
echo Plan assign-to-parent-invariant, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/assign-to-parent-invariant.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/assign-to-parent-invariant___empty.log
echo Plan array2, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/array2.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/array2___empty.log
echo Plan array5, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/array5.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/array5___empty.log
echo Plan array6, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/array6.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/array6___empty.log
echo Plan array9, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/array9.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/array9___empty.log
echo Plan failure-type1, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/failure-type1.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/failure-type1___empty.log
echo Plan failure-type2, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/failure-type2.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/failure-type2___empty.log
echo Plan failure-type3, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/failure-type3.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/failure-type3___empty.log
echo Plan failure-type4, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/failure-type4.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/failure-type4___empty.log
echo Plan array-in-loop, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/array-in-loop.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/array-in-loop___empty.log
echo Plan Increment-test, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/Increment-test.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/Increment-test___empty.log
echo Plan Increment-test2, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/Increment-test2.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/Increment-test2___empty.log
echo Plan repeat1, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/repeat1.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/repeat1___empty.log
echo Plan repeat3, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/repeat3.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/repeat3___empty.log
echo Plan repeat4, script empty
plexiltest -q -d tests/edu/umn/crisys/plexil/test/resources/Debug.cfg -p tests/edu/umn/crisys/plexil/test/resources/repeat4.plx -s tests/edu/umn/crisys/plexil/test/resources/empty.psx -L tests/edu/umn/crisys/plexil/test/resources > tests/generated/oracle/repeat4___empty.log
