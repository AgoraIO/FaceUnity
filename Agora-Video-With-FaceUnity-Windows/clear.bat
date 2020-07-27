rmdir Debug /S /Q
rmdir Release /S /Q
rmdir ipch /S /Q
rmdir .vs /S /Q
pushd language

pushd AgoraFaceUnityTutorial
rmdir Debug /S /Q
rmdir Release /S /Q
popd

pushd bin
rmdir Debug /S /Q
rmdir Release /S /Q
popd
pause