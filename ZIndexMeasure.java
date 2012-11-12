import ij.IJ;



import ij.ImagePlus;
import ij.WindowManager;
import java.util.concurrent.TimeUnit;
import mmcorej.CMMCore;

import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.MMException;

public class ZIndexMeasure implements MMPlugin {
	public CMMCore core_;
	public myCalculator mCalc = null;
	public MMStudioMainFrame gui_;
	private myGUI mygui_;
	private static ZIndexMeasure instance_;

	private String zstage_;
	private String xystage_;

	public double currxpos_ = 0;
	public double currypos_ = 0;
	public double currzpos_ = 0;
	 
	// MyAnalyzer
	public int isSetScale = 0;
	public int isInstalCallback = 0;
	public int isAcquisitionRunning = 0;
	public int isCalibration = 0;
    public int F_R_flag = 1;
    public double Mstep =100;
    
	public static ZIndexMeasure getInstance() {
		return instance_;
	}


	public void setApp(ScriptInterface app) {

		gui_ = (MMStudioMainFrame) app;
		core_ = gui_.getMMCore();
		instance_ = this;
		mygui_ = new myGUI();
		mygui_.GUIInitialization();

		xystage_ = core_.getXYStageDevice();
		zstage_ = core_.getFocusDevice();

		try {
			currxpos_ = core_.getXPosition(xystage_);
			currypos_ = core_.getYPosition(xystage_);
			currzpos_ = core_.getPosition(zstage_);	
		} catch (Exception e1) {
			mygui_.log("GET POSTION ERR");
		}

		mCalc = new myCalculator();
		mygui_.log("mCalc ini ok");

	}

	// (new Thread(new Runnable() { @Override public void run() { test1(); }
	// })).start();
	public void StartCalibration() {
		mygui_.log("Calibration Start......Checking up in IJ log for more infomation");
		if (isSetScale == 0) {
			mygui_.log("Setscale first!");
			return;
		}
		if (gui_.getAcquisitionEngine().isAcquisitionRunning()) {
			gui_.getAcquisitionEngine().stop(true);
		}
		if (gui_.isLiveModeOn()) {
			gui_.enableLiveMode(false);
		}
		double temp;
		double detal;
		mCalc.SetBitDepth(32);
		try {
			currzpos_ = core_.getPosition(zstage_);
			for (int z = 0; z < mygui_.calPos_.length; z++) {
				setZPosition(mygui_.calPos_[z]);
				temp = core_.getPosition(zstage_);
				detal = mygui_.calPos_[z] - temp;
				if (detal > 0.002 || detal < -0.002) {
					IJ.log(String.format(
							"Warning:set z position at%f,return %f detal =%f",
							mygui_.calPos_[z], temp, detal));
				}
				IJ.log(String.format("Calibrating:%d/%d\r\n", z,
						mygui_.calPos_.length));
				Object[] ret_ = null;
				double[] outpos = new double[2];
				gui_.snapSingleImage();
				ret_ = mCalc.Calibration(WindowManager.getCurrentImage()
						.getProcessor().getFloatArray(), mygui_.getROI(), z);
				outpos[0] = ((double[]) ret_[0])[0];
				outpos[1] = ((double[]) ret_[0])[1];
				mygui_.reSetROI((int) outpos[0], (int) outpos[1]);
				IJ.log(String.format("xpos:%f--ypos:%f\r\n", outpos[0],
						outpos[1]));
			}
			setZPosition(currzpos_);// turn back to the first place,Always 5
			gui_.snapSingleImage();
			mygui_.log("Calibration OK......");
		} catch (Exception e) {
			mygui_.log("Calibration False! god knows why......" + e.toString());
			e.printStackTrace();
		}
		try {
			this.firstRun();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isCalibration = 1;
		mCalc.SetBitDepth(16);

	}

	@SuppressWarnings("deprecation")
	public void dispose() {
		// TODO Auto-generated method stub
		mCalc.DeleteData();
		if (isInstalCallback == 1) {
			gui_.getAcquisitionEngine().removeImageProcessor(MyAnalyzer.class);
			isInstalCallback = 0;
			mygui_.log("Processor dettached.");
		}

	}

	public void show() {
		 
		if ((!gui_.getAcquisitionEngine().isAcquisitionRunning())
				&& (!gui_.isLiveModeOn())) {
			gui_.enableLiveMode(true);
			mygui_.Live.setText("Stop Live");
		}
	}

	public void configurationChanged() {
	}

	public String getDescription() {
		return null;
	}

	public String getInfo() {
		return null;
	}

	public String getVersion() {
		return null;
	}

	public String getCopyright() {
		return null;
	}

	/*
	 * install call back
	 */

	@SuppressWarnings("deprecation")
	public void InstallCallback() {

		if (isCalibration == 0) {
			mygui_.log("Start Calibration first!");
			return;
		}
		/*
		 * if (isInstalCallback == 1) {
		 * mygui_.log("Call back is installed! mission abort"); } else {
		 */
		gui_.getAcquisitionEngine().addImageProcessor(MyAnalyzer.class);
		isInstalCallback = 1;
		mygui_.log("Call back install,Start capture...");
		// }

		if (!gui_.getAcquisitionEngine().isAcquisitionRunning()) {
			gui_.getAcquisitionEngine().enableFramesSetting(true);
			gui_.getAcquisitionEngine().setSaveFiles(true);
			gui_.getAcquisitionEngine().setRootName(mygui_.StoragePath_);
			gui_.getAcquisitionEngine().setFrames(mygui_.frame2Acq_,
					mygui_.TimeIntervals_);
			try {
				gui_.getAcquisitionEngine().acquire();
			} catch (MMException e) {
				mygui_.log("Image Acquistion False");
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void UninstallCallback() {

		if (isInstalCallback == 1) {
			gui_.getAcquisitionEngine().removeImageProcessor(MyAnalyzer.class);
			isInstalCallback = 0;
			mygui_.log("Call back uninstal,Stop capture");
		} else {
			mygui_.log("Repeat!");
		}
	}

	public void setXPosition(double xpos) throws Exception {

		core_.setXYPosition(xystage_, xpos, this.currypos_);
		TimeUnit.MILLISECONDS.sleep(mygui_.sleeptime_);
	}

	public void setYPosition(double ypos) throws Exception {
		core_.setXYPosition(xystage_, this.currxpos_, ypos);
		TimeUnit.MILLISECONDS.sleep(mygui_.sleeptime_);
	}

	public void setZPosition(double zpos) throws Exception {
		core_.setPosition(zstage_, zpos);
		TimeUnit.MILLISECONDS.sleep(mygui_.sleeptime_);
	}

	public void debug() throws Exception {

		/*
		 * mygui_.log(String.format("#xpos:%f,ypos:%f,zpos:%f",currxpos_,currypos_
		 * ,currzpos_)); setXPosition(currxpos_); setYPosition(currypos_);
		 * setYPosition(currzpos_);
		 * 
		 * int len = mygui_.calPos_.length; double step = mygui_.zStep_; double
		 * xy[] = new double[2];
		 * 
		 * int xdebug =0; int ydebug =0; int zdebug =0;
		 * 
		 * if(mygui_.checkBoxX.isSelected()){ xdebug =1; }
		 * if(mygui_.checkBoxY.isSelected()){ ydebug =1; }
		 * if(mygui_.checkBoxZ.isSelected()){ zdebug =1; }
		 * 
		 * 
		 * if(xdebug == 1 ){ IJ.log(",,,,,"); IJ.log("Xbegin,,,,");
		 * IJ.log(",,,,,");
		 * IJ.log(String.format("#total:%f,	,	step:%f,	,	,	std/pixel=	,	std/nm=,"
		 * ,len*step,step)); IJ.log(",,,,,");IJ.log(String.format(
		 * "XSet/um,	XReal/um,	ScreenX/pixel,	ScreenXPolyfit/pixel,	ScreenXDetal/pixel,	RealXDetal/nm,	ScreenY/pixel,	XDetal/um"
		 * )); for(int i = 0;i< len ;i++){
		 * setXPosition(currxpos_+Math.cos(i*step)); if (i>10 && i<(len -10)){
		 * double x = core_.getXPosition(xystage_); xy =
		 * getGosseCenter(mygui_.getCalTimes_);
		 * IJ.log(String.format("%f,%f,%f,,,,%f,%f", currxpos_+i*step, x, xy[0],
		 * xy[1], x-currxpos_-i*step)); } } setXPosition(currxpos_);
		 * setYPosition(currypos_); }
		 * 
		 * 
		 * if(ydebug ==1){ IJ.log(",,,,,"); IJ.log("Ybegin,,,,");
		 * IJ.log(String.format
		 * ("	#total:%f,	,	step:%f,	,	,		std/pixel=	,	std/nm=,",len*step,step));
		 * IJ.log(",,,,,");IJ.log(String.format(
		 * "	YSet/um,	YReal/um,	ScreenY/pixel,	ScreenYPolyfit/pixel,	ScreenYDetal/pixel,	RealYDetal/nm,	ScreenX/pixel,	YDetal/um"
		 * ));
		 * 
		 * for(int i = 0;i<len ;i++){ setYPosition(currypos_+Math.cos(i*step));
		 * if (i>10 && i<(len -10)){ double y =
		 * this.core_.getYPosition(xystage_); xy =
		 * getGosseCenter(mygui_.getCalTimes_);
		 * IJ.log(String.format("%f,%f,%f,,,,%f,%f", currypos_+i*step, y, xy[1],
		 * xy[0], y-currypos_-i*step)); } } setXPosition(currxpos_);
		 * setYPosition(currypos_); }
		 * 
		 * 
		 * if(zdebug ==1){ IJ.log("Zbegin"); for(int i = 0;i<len;i++){
		 * setZPosition(mygui_.calPos_[i]); double zpos =
		 * core_.getPosition(zstage_); mygui_.calProfile_[i] =
		 * GetCalibration(mygui_.getCalTimes_);
		 * IJ.log(String.format("Calibrating:%d/%d--ZSet:%f,ZReal:%f,Detal:%f"
		 * ,i,len,mygui_.calPos_[i],zpos,mygui_.calPos_[i] -zpos)); }
		 * setZPosition(currzpos_); IJ.log(String.format("GetPosition start"));
		 * IJ.log(String.format(
		 * "ZSet/um,	ZReal/um,	ZGet/um, Xpos/um, 	Ypos/um	,	ZDetal/um"));
		 * for(int i = 0;i<len;i++){//get XYZPostion
		 * setZPosition(mygui_.calPos_[i]); double zpos =
		 * this.core_.getPosition(zstage_); double []pos =
		 * getXYZPositon(i,mygui_.calProfile_,mygui_.calPos_);
		 * IJ.log(String.format("%f,%f,%f,%f,%f,%f",mygui_.calPos_[i], zpos,
		 * pos[2],mygui_.roi_rectangle.x+pos[0],mygui_.roi_rectangle.y+pos[1],
		 * zpos-pos[2])); } }
		 */
	}

	private void firstRun() throws Exception {// to verify if this stuff
		// workable
		mygui_.log("Test begin......checking out the IJ log for move infomation.");
		int len = mygui_.calPos_.length;
		double pos[] = new double[4];
		IJ.log(String.format("Testing:\r\n#index,#real,#get,#detal"));
		for (int i = 0; i < len; i++) {// get XYZPostion
			setZPosition(mygui_.calPos_[i]);
			double zpos = core_.getPosition(zstage_);
			pos = getXYZPositon();
			mygui_.dataSeries_.add(i, pos[2]);
			IJ.log(String.format("%d,%f,%f,%f", i, zpos, pos[2], zpos - pos[2]));
		}
		setZPosition(currzpos_);// turn back to the first place,Always 5
		mygui_.log("Test over ");
	}

	// -----------------------------------------------------------------------------------------DEBUG
	public double[] getXYZPositon() {
		gui_.snapSingleImage();
		ImagePlus imp = WindowManager.getCurrentImage();
		Object[] ret = null;
		ret = mCalc.GetZPosition(imp.getProcessor().getFloatArray(),
				mygui_.getROI());
		return (double[]) ret[0];
	}
}