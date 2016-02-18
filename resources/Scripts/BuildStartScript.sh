#!/bin/sh

echo "#!/bin/sh" >> ~/StartDSWorkbench.sh
echo "DSWB_LOCATION=`pwd`" >> ~/StartDSWorkbench.sh
echo "cd \$DSWB_LOCATION" >> ~/StartDSWorkbench.sh
echo "sh ./DSWorkbench" >> ~/StartDSWorkbench.sh
chmod +x ~/StartDSWorkbench.sh
