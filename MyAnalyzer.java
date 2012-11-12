import mmcorej.TaggedImage;
import org.micromanager.api.TaggedImageAnalyzer;

public class MyAnalyzer extends TaggedImageAnalyzer {
	private int frame = 0; // counter
	private ZIndexMeasure main;
	private myGUI myGUI_;
	private int counter = 0;
	private double curForce = 0;

	protected void analyze(TaggedImage taggedImage) {

			myGUI_ = myGUI.getInstance();
			main = ZIndexMeasure.getInstance();
			
			if(taggedImage.pix != null){
			GetPosition(frame, taggedImage);
			frame++;
			}			

	}

	public void GetPosition(int index_, TaggedImage taggedImage) {

	 	Object[] dpos = main.mCalc.GetZPosition(taggedImage.pix,
				myGUI_.getROI());
		double pos[] = (double[]) dpos[0];

		myGUI_.resultSave[index_][0] = index_;
		myGUI_.resultSave[index_][1] = pos[0];
		myGUI_.resultSave[index_][2] = pos[1];
		myGUI_.resultSave[index_][3] = pos[2];

		myGUI_.dataSeries_.add(index_, pos[2]);

		if (counter < myGUI_.frameCalcForce_) {
			myGUI_.calcForce[counter] = pos[1];
			myGUI_.resultSave[index_][4] = curForce;
			counter++;
		}
		if (counter == myGUI_.frameCalcForce_) {
			// /opt: l p t Length
			double[] force = main.mCalc
					.GetForce(myGUI_.calcForce, myGUI_.fOpt_);
			curForce = force[0];
			myGUI_.resultSave[index_][4] = curForce;
			if(main.F_R_flag == 1){
				 (new Thread(new Runnable() { @Override public void run() { 
					 try {
							double currMP285zpos = main.core_.getPosition("MP285 Z Stage");
							main.core_.setPosition("MP285 Z Stage", currMP285zpos - main.Mstep);} 
						catch (Exception e) {
							myGUI_.log("Set MP285 ZStage ERR"+ e.toString());
						}	
				 
				 }
				 })).start();
			}
			counter = 0;
		}

		myGUI_.msg1.setText(String.format(
				"index = %d # xpos = %f # ypos = %f # zpos = %f # force/pN=%f",
				index_, pos[0], pos[1], pos[2], curForce));

		myGUI_.reSetROI((int) pos[0], (int) pos[1]); 

	}

}
