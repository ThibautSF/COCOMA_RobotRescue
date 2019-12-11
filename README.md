# COCOMA_RobotRescue

## Install
```
git clone https://github.com/ThibautSF/COCOMA_RobotRescue.git
cd COCOMA_RobotRescue/
git submodule init
git submodule update
```

## Compile

Informations comes from `rcrs-manual.pdf`.

### RCRS-server
```
cd rcrs-server
gradle clean
gradle completeBuild
```

### RCRS-adf
```
cd rcrs-adf-stss
java -jar ./library/rescue/adf/adf-core.jar -compile
```

## Run

Informations comes from `rcrs-manual.pdf`.

### 1 RCRS-server (project maps)

Map with one fire fighter:
```
./start-comprun.sh -m ../../maps/custom_map_onefirefighter/map/ -c ../../maps/custom_map_onefirefighter/config/
```


Map with four fire fighters:
```
bash start-comprun.sh -m ../../maps/custom_map/map/ -c ../../maps/custom_map/config/
```


### 2 RCRS-adf (project agents)

First learning agents:
```
sh launch.sh -mc config/module_stss_light.cfg -all
```

Learning agents extention:
```
sh launch.sh -mc config/module_stss_light2.cfg -all
```

