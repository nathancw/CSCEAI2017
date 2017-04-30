package puzzlegame.assignment8;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;


/*--------------------------------------------------------
 * 
 * For my Agent, I first modified the reflex agent code to perform its actions differently.
 * I made changes such as: implementing A* search in its movement and modifying its aggressive behavior.
 * Next, I used a genetic algorithm on the modified (improved) neural agent to find the
 * best set of weights for the agent. The best weights were chosen as the start point for the agent.
 * 
 * Most of my code has been sourced from Michael Gashler's Neural Agent, LayTahn, Neural Net code. Likewise,
 * some of my bomb throwing mechanics have inspiration from Winner2015b. The Winner2015b's agent is overly
 * aggressive and causes agents to dodge bombs that aren't even landing towards them. This aggressive bomb 
 * throwing was key change to making the neural agent stronger. Combine this with genetic algorithm and you
 * have agents who can continually beat other old neural agents.
 *--------------------------------------------------------*/


public class WebbNathaniel implements IAgent{

	///////////BEGIN NEURAL AGENT /////////////////////

	int index; // a temporary value used to pass values around
	NeuralNet nn;
	double[] in;
	boolean heuristicFound = false;
	UCS ucs;
	float lowestVal = -100;
	int accum;
	
	WebbNathaniel() {
		//double[] weights;
		//double[] weights = {0.033101794598140576, 0.037885967845811296, -0.010245821016055846, -0.005325714499720422, 0.02609789360537013, -0.051314335380896046, 0.0131376512920218, -0.004074246713508772, -0.03602663504005055, 0.05582829203673031, 0.013194125217806031, 0.03995132978984181, 0.014489825140693632, 0.03331587372577749, 0.014183776752876208, -0.03654507436259152, 0.05856180044886969, -0.01600106789520113, -0.018827240046945987, 0.01106146978531165, -0.02904078090461616, -0.004047356508723848, -0.01554378389817107, -0.008662871627529782, -0.009574249212672968, -0.0014097343651446389, -0.008690556357140177, -0.007730930702677045, 0.006105659867405041, 0.05249324615214148, 0.03284119370856591, 0.03870358073203198, -0.016906520491366806, 0.03405075067414181, -0.008521092923461603, 0.025364386622247118, 0.047009364802602094, -0.0261261528764004, -0.0548921848317076, -0.04561914641977185, 0.0023022531066960207, 0.018588635250707766, -0.021028458856189078, 0.0041117259109367515, 0.022431335539909704, 0.03578470535765151, 0.011098931077523183, 0.04376252065622871, 0.02387560145493179, -0.01235124312723438, -0.023967910443034136, 0.0011880487379009395, 0.03801928053511724, -0.01764268660756441, -0.009169757504298352, 0.006410184759688387, 0.01935886111095156, -0.007485526548319205, 0.009772892920694807, 0.003742341746080831, -0.006837602897118956, 0.06635734034046235, 0.0663612422457823, 0.01807402268480109, -0.02433522140223349, 0.0032304445912384565, -0.019796163436163517, -0.007383310632922518, 0.03391830461726024, 0.04360649582850156, 0.02667156475592968, -0.006748853412839305, 0.0633710431010912, 0.056918015907448265, 0.0011424424981043727, -0.022981407647789744, 0.03483878671615743, -0.028938394563472424, 0.00740916607890347, -0.08399550822292906, 0.014675829468644723, -0.04827468233465803, -0.016626178665516366, -0.0026409621515945505, -0.033970121687917645, 0.01846382710892023, 0.010404585724005239, 0.024918928563352684, -0.024353141900611466, 0.015032830705879657, 0.018765621357363944, -0.0339403101845969, -0.0018350574968773944, 0.049648359200637183, 0.02492949111252097, -0.021075181843117692, 0.03177457650912261, -0.03040821988640727, 0.0860020035854431, -0.06670875370407235, -0.016696019142308137, 0.004938106357220146, 0.0015205920599384347, -0.007925436658267034, 0.026728144617340282, 0.005237369431255913, -0.004705448219475299, 0.002080871635293955, 0.009318824164744317, 0.04254724566422331, -0.008304417350881053, -0.015321639390109183, -0.0033974381169097003, 0.45142089789930884, -0.05571097210559532, 0.06330942511001778, 0.015299058920039847, 0.008594349824234212, -0.00584446362616833, -0.0031815366559280705, 0.0020699835438378225, 0.0012920526432923607, -0.0170495325453104, -0.029021260453762115, -0.04467129628852391, 0.005851489599067348, 0.001062941589005009, 0.02589193285626173, -0.037507470465699716, 0.025750690908390054, 0.03588432553773679, 0.03318747938929816, 0.046369230496549785, -0.021737235617078153, 0.03446222146527446, 0.00904234205524542, 0.004647070666584825, -0.04001480070478174, 0.019752411330411904, -0.0017652313544024628, -0.048308481721174024, -0.028559940161052354, 0.022416320201267687, 0.027709735718042704, 0.014667890960857792, 0.005011956400496257, -0.05398769051595626, 0.04564840249465279, 0.002376947680171644, 0.006121341128658331, -0.01272766561798929, 0.01438602478943998, 0.02469093905450042, 7.429952843194479E-4, -0.05751894254343031, 0.04562072010578245, -0.035857559178683836, -0.01941948723321831, -0.07160594101392095, -0.043940745117054744, 0.02663503823766137, 0.033186205473539676, -0.051770598694217275, -0.00512963017872312, -0.03062667677809932, 0.03472697034054046, 0.013677206051888192, 0.144968099982142, -0.023190400253054338, -0.011730925272374506, 0.003413021565261132, -0.03344687061751878, 0.013538425846246094, -0.022183752750521705, 0.010486521282051683, 0.023541068400944203, 0.014708941045257665, -0.03942414237734884, -0.005018092150268439, 0.0031071895256851814, -0.02874596715309553, -0.024811519374128028, 0.004692326610849445, 0.009690318186421206, 0.04713381863590584, -0.028572661407483622, -0.008591605074599877, -0.01050665462601138, -0.029639870819968626, 0.02001506220550968, -0.011604418526691867, 0.02116411442203858, 0.03226548131525112, -0.0032210576039093434, -0.017408937694982602, 0.004565708776401782, -0.027722337340256812, -0.03131435106370965, -0.03446825198567018, 0.025974590400805322, 0.04538214955205185, -0.020207641537758534, 0.010528615636385682, -0.0013467575221006876, 0.02567253446287197, 0.0540328788457866, -0.021633457876455375, 0.05838996631135127, 0.030253759822884037, 0.016562609935255556, -8.300098751963401E-4, -0.011617438753422409, 0.04210332709666795, 0.048301814851713434, -0.016977168063844663, 0.001092007669816094, 0.03118547386387713, 0.014517630800882938, 0.007800701862217221, 0.053612766204767515, 0.0012572108417527273, 0.014577252343278875, 0.0018578535971752325, 0.0050696686213194155, -0.03260110355012511, 0.027363470514176425, 0.010734096868695132, 0.02005159964393262, -0.048532996623713846, 0.013515994185717441, -0.044225856633565645, -0.038317114070664966, 0.021747848069325927, 0.039689956136373584, 0.015518041537775362, 0.012303146258509525, -0.0025677751442162457, 0.011261648883798903, 0.05271450879249941, -6.258421045728557E-4, -0.009173238566344622, 0.024836474150307927, -0.021591421349880877, 0.005635337760286306, -0.008053948887021302, 0.025614091495329512, -5.430009657451895E-4, 0.022012657239611943, -0.01655753016407906, -0.015042092975940089, 0.010450853227475817, -0.060500025630312496, 0.04100146894207475, -0.0594579202831297, -0.030404141521393632, 0.003810631506517905, -0.04828718826691137, -0.00824376268919517, -0.018900346263147596, -0.01753651548691498, 0.009934155041176876, 0.04499212575743443, 4.600755746631654E-4, 0.015761038364398374, 5.740484776593654E-4, -0.0332142605404036, -0.021569969334981363, 0.011500425127884408, -0.01771070993944168, -1.9727319749026387E-4, 0.0674148495613735, 0.016132956526673, -0.04947918188958408, -3.968718323952612E-4, 0.03398338071071778, 0.05300693899239984, -0.050994478195426615, -0.009921204237936748, -0.018595330990398784, -0.023699636992463243, 0.029911443955383553, 0.004810592200276789, -0.0262311020267402, 0.01822357671619329, -0.04790459309274039, 0.052389518860816935, -0.008745500624445483, -0.019466624712343745, 0.019802451986000766, 0.015002235599486269, -0.035692988735743204};
		
		double [] weights = {0.03446056062651865, 0.003916796488313233, 0.016236296796816888, 0.4374665175252196, 0.04115761702270067, -0.008400286821926901, -0.040912664362997814, 0.029413276127786332, -0.03937296789524518, 0.030829931684467, 0.02830929460531971, 0.055319333187746934, 0.01587637575887888, 0.0895272407494908, 0.011506143278303094, 0.047396394160531145, -0.02427981844459351, 0.011017944575728678, -0.03868732275784303, 0.029967864472544088, -0.02599375045488274, -0.005047801831291337, -0.029496956074468648, -0.02994166207692087, 0.002903871254979763, 0.04028895835952639, 0.044720142133443885, -0.02098664747509813, -0.004151847690491715, 0.03088000043753724, 0.022338107110118716, -0.03979609498162181, 0.025305166354137307, 0.028661539490737128, -0.009085879676572331, 0.02168327733926469, -0.008799488979796656, 0.03919466939091406, -0.04010143832534754, 2.3760564514719318E-4, 0.023122085110782866, 0.0039574558081371825, -0.0011963911442050596, 0.04416699335238182, 0.032465008219144095, 0.029859688083641756, 5.362485537443826E-4, 0.012933584577690253, -0.023959734057503828, 0.008568287126042321, -0.03548899841639717, -0.007659304766079445, 0.04152998540081651, 0.021454500411313512, -0.002344695632702547, -0.0029793436318363508, -0.027125915955400103, 0.013089869696581904, -0.008171945383889266, -0.042338449290052024, -0.03761481508723173, 0.007370100134302528, -0.01584007551474565, -0.028029185058032174, -0.011561408900164733, 0.01922853068459878, -0.012470130584811013, -3.924202384147684E-4, 0.023139392132962983, 0.008739682072884877, -0.0067758812098017, -0.06512988785897977, 0.006995961654084861, -0.002464672242770833, 0.017244676139192918, 0.05421880087668381, -0.019715142842988236, -0.010161580646699125, -0.02133867516473342, -0.06571810787596986, -0.02453348647390667, -0.027068922204771483, 0.053380115163836644, -0.02318411697136097, -0.00783670087615676, -0.049576982915991524, 0.05210701885478821, -0.013907011473903738, 0.02049399585303981, -0.0155304358203207, -0.019359159842685502, -0.008405790875769426, 0.022776312936501043, 0.005744930893409653, 3.1274735228147964E-4, 0.03155916708597468, 0.026885022215874833, -0.028427420691061474, 0.01596766972196786, 0.03099580491578005, 0.0038392021536011755, 0.006109956301568606, -3.981816182078401E-4, -0.047658030213598206, -0.020590683436130793, 0.0022889372463732677, 0.006858131133600876, -0.014955700003986416, 0.0018674781089782094, -0.023082446879168463, 0.0019555732221436064, 0.01785689000317817, -0.005232630098000992, -0.018043113066196586, -0.003723659611663681, 0.03306190644747359, 0.025977972927779973, 0.04562008660732913, 0.04435286336032021, 0.03107850722615532, 0.007234169480338417, 0.022049152226104245, 0.03357678291591613, 0.021955358841333147, 0.031288347063735775, -0.029758861373628893, 0.008868537851860354, -0.04275452100301823, -0.004378773447184155, 0.02145583370322195, -0.0259633181618801, -0.03642398163676568, -0.028103531838416247, 0.05947860227833595, -0.044540318442241024, -0.01767366342563467, 0.037875041489263364, -0.012023128425409434, -0.012843798653035454, -0.009116161572968491, -0.038431971418429814, 0.01381025479359063, 0.006263812489154815, -0.007776197553510869, -0.04184174954270159, 0.03430340014948133, 0.0051710500140362565, 0.0010809472289394393, -0.028088779738828853, -0.006248896961783293, 0.01781631760381707, -0.02448359532737412, 0.010429677668066033, 8.330577308966899E-4, -0.0012709801068211978, -0.0362425154000316, 0.013095178021898891, -0.041869537133779486, -0.019319338838952756, -0.03429854025301425, -0.04717290141379684, -0.0162001443422039, -0.06375869905555309, -0.004908890894699204, 2.32030942880958E-4, 0.024221849617445196, 0.03198851333827037, 0.001105275563774788, 0.03128670581334151, -0.009371156401720752, -0.002293904424051558, -3.4913618664013824E-4, -0.05132294939646244, -0.014483408815440602, -0.03506429885379652, 0.0192473261330374, -0.005490446572879691, -0.010957500493814117, -4.571574814163868E-4, 0.045045329515014415, -0.04345032648802892, 0.04374895163214943, 0.01208948787704769, -0.0012247569231278095, -0.001802880184282764, 0.008374053514827942, -0.016208541353200795, -0.011397405509244203, 0.031571053040742765, -0.02315086708198198, 0.02340216008500709, 0.02222052262377621, 0.01728386820423929, -0.04634995391164071, 0.074054661913772, -0.03144993642119331, 0.005151156963967788, 0.0016192773932238599, -0.01127792648244404, -0.003997107583203943, 0.051269959220898983, -0.010982501999772125, -0.03223630680586486, 0.06409972273582104, -0.01239209084557152, 0.047264973544047816, -0.030360145111719906, 0.029903345487951007, 0.042224857254742516, 0.04684597605065077, -0.009368092147149183, 0.050658120448061915, -0.002796938605276061, 0.004247500497163166, -0.044307154878487624, -0.033866652210646817, -0.042906902001402424, -0.035112150475437885, 0.029413732911391814, 0.009009660281830149, -0.04365487471374505, 0.004913714248909507, 0.01626314411712664, -0.003767246937791229, 0.06460537597414213, 0.029782406869747535, -0.02379451519916512, -0.057927525665770044, -0.034338757577540735, 0.08484251637985132, 0.0017761977129069734, -0.05181726092433174, 0.027239285951049894, 0.023655221867956305, 0.014962659434576456, -0.009978557260060838, 0.022077758202693858, -0.04795060656515949, -0.002793239233658185, 0.02243150528495839, -0.03091826079514084, -0.04557088996347596, 0.01820355573825933, -0.025621412463753632, 0.026063190309608956, -1.0106121586327478E-4, 0.009768239814229232, 0.017972610773778135, 0.011455341703745878, 0.035123030493922236, 0.052997226742031917, -0.017710084734789817, -0.012258531633899481, 0.06503591363466805, -0.04885727562085322, 0.008905688898537737, 0.0063772180959666015, 0.05464298967678999, -0.021216158682786933, -0.007996054908286658, -9.87530076755474E-4, 0.08712629544556279, -0.011482503309424604, -0.0159261294181955, 0.030796760295009808, -0.026459740900496777, -0.03682448818239723, 0.05578365709060043, 0.002638483482515392, -0.008138526960522663, -0.0435094480059896, 0.053484339796975526, -0.013029244378885495, -0.009316631619278473, 0.012906596151086339, -0.04185130434729234, -0.053969007928860516, -0.043849268690142204, 0.04187617839889352, -0.04230526031734079, -0.02133076850321897, 0.004594742105420671, 0.031346052335494284, -0.03785059832459143, -0.02120325554495153, -0.06669193253094202, -0.001109378147304152, 0.028407117420955294, 0.013846163128544923, 0.004392588732573721, -0.003944435906972717};
		
	/*	try {
				weights = Evolution.evolveWeights();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		
			in = new double[20];
			nn = new NeuralNet();
			nn.layers.add(new LayerTanh(in.length, 8));
			nn.layers.add(new LayerTanh(8, 10));
			nn.layers.add(new LayerTanh(10, 3));
			setWeights(weights);
			ucs = new UCS(true);
			accum = 0;
	}
	
	
	WebbNathaniel(double[] weights) {
		
		in = new double[20];
		nn = new NeuralNet();
		nn.layers.add(new LayerTanh(in.length, 8));
		nn.layers.add(new LayerTanh(8, 10));
		nn.layers.add(new LayerTanh(10, 3));
		setWeights(weights);
		ucs = new UCS(true);
		accum = 0;
	}
	
	public void reset() {
	}
	
	/// Returns the number of weights necessary to fully-parameterize this agent
	int countWeights() {
		int n = 0;
		for(int i = 0; i < nn.layers.size(); i++)
			n += nn.layers.get(i).countWeights();
		return n;
	}
	
	
	/// Sets the parameters of this agent with the specified weights
	void setWeights(double[] weights) {
		if(weights.length != countWeights())
			throw new IllegalArgumentException("Wrong number of weights. Got " + Integer.toString(weights.length) + ", expected " + Integer.toString(countWeights()));
		int start = 0;
		for(int i = 0; i < nn.layers.size(); i++)
			start += nn.layers.get(i).setWeights(weights, start);
	}
	
	
	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}
	
	float nearestBombTarget(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getBombCount(); i++) {
			float d = sq_dist(x, y, m.getBombTargetX(i), m.getBombTargetY(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}
	
	float nearestOpponent(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if(m.getEnergyOpponent(i) < 0)
				continue; // don't care about dead opponents
			float d = sq_dist(x, y, m.getXOpponent(i), m.getYOpponent(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}
	
	void avoidBombs(Model m, int i) {
		if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			float dx = m.getX(i) - m.getBombTargetX(index);
			float dy = m.getY(i) - m.getBombTargetY(index);
			if(dx == 0 && dy == 0)
				dx = 1.0f;

			float newX = Model.XFLAG;
			float newY = Model.YFLAG;

			findBestDestination(m,i,newX,newY);
			//	m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
		}
	}
	
	void beDefender(Model m, int i) {
		
		float myX = m.getX(i);
		float myY = m.getY(i);
		// Find the opponent nearest to my flag
		nearestOpponent(m, Model.XFLAG, Model.YFLAG);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
			float dx = myX - enemyX;
			float dy = myY - enemyY;
			float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
			dx *= t;
			dy *= t;
			
			// Stay between the enemy and my flag
			findBestDestination(m,i,0.6f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));
			//m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));
	
			// Throw bombs
			if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS){
				m.throwBomb(i, enemyX, enemyY);
			}
			//Code below has taken inspiration from Winner2015b and modified from Winner2015b.
			else if (Math.sqrt(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)))  < Model.MAX_THROW_RADIUS + (Model.BLAST_RADIUS * 0.25 ) ) {
				float factor = (float) (Model.MAX_THROW_RADIUS / Math.sqrt(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i))) );
				float throwX = dx * factor + enemyX;
				float throwY = dy * factor + enemyY;
				//System.out.println("---------------------------------------------------");
				m.throwBomb(i,throwX,throwY);
			}
		}
	
		// Try not to die
		avoidBombs(m, i);
	}
	
	void beFlagAttacker(Model m, int i) {
		// Head for the opponent's flag
		findBestDestination(m,i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS, Model.YFLAG_OPPONENT);
		// Shoot at the flag if I can hit it
		if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
			m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
		}
	
		// Try not to die
		avoidBombs(m, i);
	}
	
	void beAggressor(Model m, int i) {
	
		float myX = m.getX(i);
		float myY = m.getY(i);
		
		shootFlag(m,i);
		// Find the opponent nearest to me
		nearestOpponent(m, myX, myY);
		
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
	
			if(m.getEnergySelf(i) >= m.getEnergyOpponent(index)) {
	
				// Get close enough to throw a bomb at the enemy
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
				findBestDestination(m,i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));
				//m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));
	
				// Throw bombs
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS){
					m.throwBomb(i, enemyX, enemyY);
				}
				//Code below has taken inspiration from Winner2015b and modified from Winner2015b.
				else if (Math.sqrt(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)))  < Model.MAX_THROW_RADIUS + (Model.BLAST_RADIUS * 0.25 ) ) {
					float factor = (float) (Model.MAX_THROW_RADIUS / Math.sqrt(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i))) );
					float throwX = dx * factor + enemyX;
					float throwY = dy * factor + enemyY;
					//System.out.println("---------------------------------------------------");
					m.throwBomb(i,throwX,throwY);
				}
			}
			
		}
	
		
		// Try not to die
		avoidBombs(m, i);
	}
	
	
	
	void shootFlag(Model m, int i){
	//	findBestDestination(m,i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS, Model.YFLAG_OPPONENT);
		
		// Shoot at the flag if I can hit it
		if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
			m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
		}
	}
	
	private void findBestDestination(Model m, int i, float f, float g) {
	
		if(lowestVal == -100)
			lowestVal = calculateLowest(m);
		
		int destX = (int) (Math.ceil(f) / 10) * 10;
		int destY = (int) (Math.ceil(g) / 10) * 10;
		
		int sX = (int) ((m.getX(i)) / 10) * 10;
		int sY = (int) ((m.getY(i)) / 10) * 10;
		
	//	System.out.println(i + " BeFlagAttacker. Sx: " + sX + " Sy: " + sY);
		Block next = ucs.uniform_cost_search(m, new Block(sX,sY,(float) 0.0,null,(float)0.0), 
				new Block(destX,destY,(float) 0.0,null,(float)0.0), lowestVal);
		
		//System.out.println("Flag: " + flagX + "," + flagY);
		//System.out.println("Next: " + next.x + ", " + next.y);
		m.setDestination(i, next.x, next.y);
		
		
	}
	
	public void update(Model m) {
	
		// Compute some features
		in[0] = m.getX(0) / 600.0 - 0.5;
		in[1] = m.getY(0) / 600.0 - 0.5;
		in[2] = m.getX(1) / 600.0 - 0.5;
		in[3] = m.getY(1) / 600.0 - 0.5;
		in[4] = m.getX(2) / 600.0 - 0.5;
		in[5] = m.getY(2) / 600.0 - 0.5;
		in[6] = nearestOpponent(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[7] = nearestOpponent(m, m.getX(1), m.getY(1)) / 600.0 - 0.5;
		in[8] = nearestOpponent(m, m.getX(2), m.getY(2)) / 600.0 - 0.5;
		in[9] = nearestBombTarget(m, m.getX(0), m.getY(0)) / 600.0 - 0.5;
		in[10] = nearestBombTarget(m, m.getX(1), m.getY(1)) / 600.0 - 0.5;
		in[11] = nearestBombTarget(m, m.getX(2), m.getY(2)) / 600.0 - 0.5;
		in[12] = m.getEnergySelf(0);
		in[13] = m.getEnergySelf(1);
		in[14] = m.getEnergySelf(2);
		in[15] = m.getEnergyOpponent(0);
		in[16] = m.getEnergyOpponent(1);
		in[17] = m.getEnergyOpponent(2);
		in[18] = m.getFlagEnergySelf();
		in[19] = m.getFlagEnergyOpponent();
	
		// Determine what each agent should do
		double[] out = nn.forwardProp(in);
	
		// Do it
		if(accum>200){
			for(int i = 0; i < 3; i++)
			{
			
				if(m.getEnergyOpponent(0) < 0 && m.getEnergyOpponent(1) < 0 && m.getEnergyOpponent(2) < 0){
					beFlagAttacker(m,i);
				}
				else{
					if(out[i] < -0.333)
						beDefender(m, i);
					else if(out[i] > 0.333)
						beAggressor(m, i);
					else
						beAggressor(m, i);
				}
			}
		}
		accum++;
	
	}
	
	float calculateLowest(Model m) {
		
		float temp;
		float lowestVal = 0;
		if(!heuristicFound){
		for(int x = 0; x < 1200; x+=10)
			for(int y =0; y < 600; y+=10){
				temp = (m.getTravelSpeed(x,y));
				if(temp > lowestVal)
					lowestVal = temp;
			}
		}
		return lowestVal;
	}

	
	
	
	/* ------------------ A* Search Code Below --------------------*/
	/* ------------------------------------------------------------*/
	
	static class Block {
	public float cost;
	Block parent;
	float x;
	float y;
	float heuristic = 0;
	Block(float x, float y, float cost, Block par, float h) {
		  this.cost = cost;
		  this.parent = par;
		  this.x = x;
		  this.y = y;
		  this.heuristic = h;
	
	}
	
	void print(){
		  System.out.println("(" + x + "," + y + ") cost : " + cost);
	}
	}
	
	static class BlockComparator implements Comparator<Block>
	{
	public int compare(Block a, Block b)
	{
		
		  Float x1 = a.x;
	      Float x2 = b.x;
	      int floatCompare1 = x1.compareTo(x2);
	
	      if (floatCompare1 != 0) {
	          return floatCompare1;
	      } else {
	          Float y1 = a.y;
	          Float y2 = b.y;
	          return y1.compareTo(y2);
	      }
	  }
		
		
	}
	static class CostComparator implements Comparator<Block>
	{
	public int compare(Block a, Block b)
	{
			if((a.cost + a.heuristic) > (b.cost + b.heuristic))
				return 1;
			else if((a.cost + a.heuristic) < (b.cost + b.heuristic))
					return -1;
		return 0;
	}
	}  
	
	
	static class UCS {
	BlockComparator comp;
	CostComparator costComp;
	PriorityQueue<Block> frontier;
	TreeSet<Block> beenThere;
	Stack<Block> path;
	boolean aStar;
	Block goal;
	float lowest;
	
	public UCS(boolean a){
		this.aStar = a;
	}
	
	private float calculateHeur(float xCurr, float yCurr, float xGoal,float yGoal) {
		float pow1 = (float) Math.pow((xCurr - xGoal),2);
		float pow2 = (float) Math.pow((yCurr - yGoal),2);
		float total = (float) ((Math.sqrt(pow1 + pow2))/lowest);
		//System.out.println("Lowest: " + lowest +  "total: " + total);
		return total;
	}
	
	public Block uniform_cost_search(Model m, Block startState, Block goal, float lowest) {
		this.lowest = lowest;
		
		boolean found = false;
		comp = new BlockComparator();
		costComp = new CostComparator();
		frontier = new PriorityQueue<Block>(costComp);
		beenThere = new TreeSet<Block>(comp);
		path = new Stack<Block>();
	  frontier.add(startState);
	  beenThere.add(startState);
	  this.goal = goal;
	  
	  while(frontier.size() > 0) {
	    Block s = (Block) frontier.remove(); // get lowest-cost state
	    
	    //s.print();
	    if(s.x == goal.x && s.y == goal.y){
	  	  goal.parent = s;
	  	  found = true;
	  	  break;
	    }
	    //
	    MoveState(m,s,10,-10); //x+10, y-10;
	    MoveState(m,s,10,0); //x+10
	    MoveState(m,s,10,10); //x+10, y+10
	    MoveState(m,s,0,10); //y+10
	    MoveState(m,s,-10,10);//x-10, y+10
	    MoveState(m,s,-10,0); //x-10
	    MoveState(m,s,-10,-10);//x-10, y-10
	    MoveState(m,s,0,-10); //y-10
	   
	  } 
	  
	  if(!found){
	  	System.out.println("Can't find route");
	  	System.out.println("Start: " + startState.x + "," + startState.y + " goal: " + goal.x + "," + goal.y);
	  	//return new Stack();
	  }
	  
	  
	  Block current = goal;
	  while(current!=null){ 	
	  	path.add(current);
	  	current = current.parent;
	  	
	  }
	  
	  //Pop one before
	 if(!(path.size() > 2))
	  return startState;
	 else
		   path.pop();
	
	  return path.pop();
	  //throw new RuntimeException("There is no path to the goal");
	}
	
	public PriorityQueue<Block> getFrontier(){
		return frontier;
	}
	
	private void MoveState(Model m, Block root, float xMove, float yMove) {
		float x = (float) (root.x+xMove);
		float y = (float) (root.y+yMove);
	//	System.out.println("Checking: " + x + " ," + y);
		if(x < 1199 && y < 599 && y > 0 && x > 0){
			
			float cost;
			if((Math.abs(xMove) + Math.abs(yMove))==20)
				cost = (float)(10/(m.getTravelSpeed(x,y))*Math.sqrt(2));//Cost is speed associated with the terrain square AND distance you will travel at that speed
			else
				cost =  (float)(10/(m.getTravelSpeed(x,y)));
			
			float heur = 0;
			if(aStar){
				heur = calculateHeur(x,y,goal.x,goal.y);
				cost = cost + root.cost;
			}
			else{
				cost = cost + root.cost;
			}
			
			
			Block child = new Block(x,y,cost,root, heur);
			Block oldChild;
		
			if(beenThere.contains(child)){ //If the new block is already in the set, then we need to check cost
				oldChild = beenThere.floor(child); //find the block with the same x,y
				if(cost < oldChild.cost) { //If the root cost + new cost is less than old cost, then update new cost and make 
			        oldChild.cost =  cost; //new parent
			        oldChild.parent = root;
			      }	
			}
			else {	//If its not in the set, add it to the set, dont care about cost
				frontier.add(child);
				beenThere.add(child);
			}	
		}
		
	}
	
	}
	
	/* ------------------------------------------------------------*/
	/* ------------------------------------------------------------*/

	
	static public class Vec
	{
		public static String toString(double[] vec) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			if(vec.length > 0) {
				sb.append(Double.toString(vec[0]));
				for(int i = 1; i < vec.length; i++) {
					sb.append(",");
					sb.append(Double.toString(vec[i]));
				}
			}
			sb.append("]");
			return sb.toString();
		}

		public static void setAll(double[] vec, double val) {
			for(int i = 0; i < vec.length; i++)
				vec[i] = val;
		}

		public static double squaredMagnitude(double[] vec) {
			double d = 0.0;
			for(int i = 0; i < vec.length; i++)
				d += vec[i] * vec[i];
			return d;
		}

		public static void normalize(double[] vec) {
			double mag = squaredMagnitude(vec);
			if(mag <= 0.0) {
				setAll(vec, 0.0);
				vec[0] = 1.0;
			} else {
				double s = 1.0 / Math.sqrt(mag);
				for(int i = 0; i < vec.length; i++)
					vec[i] *= s;
			}
		}

		public static void copy(double[] dest, double[] src) {
			if(dest.length != src.length)
				throw new IllegalArgumentException("mismatching sizes");
			for(int i = 0; i < src.length; i++) {
				dest[i] = src[i];
			}
		}

		public static double[] copy(double[] src) {
			double[] dest = new double[src.length];
			for(int i = 0; i < src.length; i++) {
				dest[i] = src[i];
			}
			return dest;
		}

		public static void add(double[] dest, double[] src) {
			if(dest.length != src.length)
				throw new IllegalArgumentException("mismatching sizes");
			for(int i = 0; i < dest.length; i++) {
				dest[i] += src[i];
			}
		}

		public static void scale(double[] dest, double scalar) {
			for(int i = 0; i < dest.length; i++) {
				dest[i] *= scalar;
			}
		}

		public static double dotProduct(double[] a, double[] b) {
			if(a.length != b.length)
				throw new IllegalArgumentException("mismatching sizes");
			double d = 0.0;
			for(int i = 0; i < a.length; i++)
				d += a[i] * b[i];
			return d;
		}

		public static double squaredDistance(double[] a, double[] b) {
			if(a.length != b.length)
				throw new IllegalArgumentException("mismatching sizes");
			double d = 0.0;
			for(int i = 0; i < a.length; i++) {
				double t = a[i] - b[i];
				d += t * t;
			}
			return d;
		}

		public static void clip(double[] vec, double min, double max) {
			if(max < min)
				throw new IllegalArgumentException("max must be >= min");
			for(int i = 0; i < vec.length; i++) {
				vec[i] = Math.max(min, Math.min(max, vec[i]));
			}
		}

		public static double[] concatenate(double[] a, double[] b) {
			double[] c = new double[a.length + b.length];
			for(int i = 0; i < a.length; i++)
				c[i] = a[i];
			for(int i = 0; i < b.length; i++)
				c[a.length + i] = b[i];
			return c;
		}

	}
	
	static public class Matrix
	{
		/// Used to represent elements in the matrix for which the value is not known.
		public static final double UNKNOWN_VALUE = -1e308; 

		// Data
		private ArrayList<double[]> m_data = new ArrayList<double[]>(); //matrix elements

		// Meta-data
		private String m_filename;                          // the name of the file
		private ArrayList<String> m_attr_name;                 // the name of each attribute (or column)
		private ArrayList<HashMap<String, Integer>> m_str_to_enum; // value to enumeration
		private ArrayList<HashMap<Integer, String>> m_enum_to_str; // enumeration to value

		/// Creates a 0x0 matrix. (Next, to give this matrix some dimensions, you should call:
		///    loadARFF
		///    setSize
		///    addColumn, or
		///    copyMetaData
		@SuppressWarnings("unchecked")
		public Matrix() 
		{
			this.m_filename    = "";
			this.m_attr_name   = new ArrayList<String>();
			this.m_str_to_enum = new ArrayList<HashMap<String, Integer>>();
			this.m_enum_to_str = new ArrayList<HashMap<Integer, String>>();
		}

		public Matrix(int rows, int cols)
		{
			this.m_filename    = "";
			this.m_attr_name   = new ArrayList<String>();
			this.m_str_to_enum = new ArrayList<HashMap<String, Integer>>();
			this.m_enum_to_str = new ArrayList<HashMap<Integer, String>>();
			setSize(rows, cols);
		}

		public Matrix(Matrix that)
		{
			setSize(that.rows(), that.cols());
			m_filename = that.m_filename;
			m_attr_name = new ArrayList<String>();
			m_str_to_enum = new ArrayList<HashMap<String, Integer>>();
			m_enum_to_str = new ArrayList<HashMap<Integer, String>>();
			copyBlock(0, 0, that, 0, 0, that.rows(), that.cols());
		}

		/// Loads the matrix from an ARFF file
		public void loadARFF(String filename)
		{
			HashMap<String, Integer> tempMap  = new HashMap<String, Integer>(); //temp map for int->string map (attrInts)
			HashMap<Integer, String> tempMapS = new HashMap<Integer, String>(); //temp map for string->int map (attrString)
			int attrCount = 0; // Count number of attributes
			int lineNum = 0; // Used for exception messages
			Scanner s = null;
			m_str_to_enum.clear();
			m_enum_to_str.clear();
			m_attr_name.clear();

			try
			{
				s = new Scanner(new File(filename));
				while (s.hasNextLine())
				{
					lineNum++;
					String line  = s.nextLine().trim();
					String upper = line.toUpperCase();

					if (upper.startsWith("@RELATION"))
						m_filename = line.split(" ")[1];
					else if (upper.startsWith("@ATTRIBUTE"))
					{
						String[] pieces = line.split("\\s+");
						m_attr_name.add(pieces[1]);
						
						tempMap.clear();
						tempMapS.clear();
						
						// If the attribute is nominal
						if (pieces[2].startsWith("{"))
						{
							// Splits this string based on curly brackets or commas
							String[] attributeNames = pieces[2].split("[{},]");
							int valCount = 0;
							
							for (String attribute : attributeNames)
							{
								if (!attribute.equals("")) // Ignore empty strings
								{
									tempMapS.put(valCount, attribute);
									tempMap.put(attribute, valCount++);
								}
							}
						}
						
						// The attribute is continuous if it wasn't picked up in the previous "if" statement
						
						m_str_to_enum.add(new HashMap<String, Integer>(tempMap));
						m_enum_to_str.add(new HashMap<Integer, String>(tempMapS));
						
						attrCount++;
					}
					else if (upper.startsWith("@DATA"))
					{
						m_data.clear();
						
						while (s.hasNextLine())
						{
							double[] temp = new double[attrCount];

							lineNum++;
							line  = s.nextLine().trim();
							
							if (line.startsWith("%") || line.isEmpty()) continue;
							String[] pieces = line.split(",");
							
							if (pieces.length < attrCount) throw new IllegalArgumentException("Expected more elements on line: " + lineNum + ".");
							
							for (int i = 0; i < attrCount; i++)
							{
								int vals   = valueCount(i);
								String val = pieces[i];
								
								// Unknown values are always set to UNKNOWN_VALUE
								if (val.equals("?"))
								{
									temp[i] = UNKNOWN_VALUE;
									continue;
								}
			
								// If the attribute is nominal
								if (vals > 0)
								{
									HashMap<String, Integer> enumMap = m_str_to_enum.get(i);
									if (!enumMap.containsKey(val))
										throw new IllegalArgumentException("Unrecognized enumeration value " + val + " on line: " + lineNum + ".");
										
									temp[i] = (double)enumMap.get(val);
								}
								else
									temp[i] = Double.parseDouble(val); // The attribute is continuous
							}
							
							m_data.add(temp);
						}
					}
				}
			}
			catch (FileNotFoundException e)
			{
				throw new IllegalArgumentException("Failed to open file: " + filename + ".");
			}
			finally
			{
				s.close();
			}
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			for(int j = 0; j < rows(); j++) {
				if(j > 0)
					sb.append("\n");
				sb.append(Vec.toString(row(j)));
			}
			return sb.toString();
		}

		/// Saves the matrix to an ARFF file
		public void saveARFF(String filename)
		{		
			PrintWriter os = null;
			
			try
			{
				os = new PrintWriter(filename);
				// Print the relation name, if one has been provided ('x' is default)
				os.print("@RELATION ");
				os.println(m_filename.isEmpty() ? "x" : m_filename);
				
				// Print each attribute in order
				for (int i = 0; i < m_attr_name.size(); i++)
				{
					os.print("@ATTRIBUTE ");
					
					String attributeName = m_attr_name.get(i);
					os.print(attributeName.isEmpty() ? "x" : attributeName);
					
					int vals = valueCount(i);
					
					if (vals == 0) os.println(" REAL");
					else
					{
						os.print(" {");
						for (int j = 0; j < vals; j++)
						{
							os.print(attrValue(i, j));
							if (j + 1 < vals) os.print(",");
						}
						os.println("}");
					}
				}
				
				// Print the data
				os.println("@DATA");
				for (int i = 0; i < rows(); i++)
				{
					double[] row = m_data.get(i);
					for (int j = 0; j < cols(); j++)
					{
						if (row[j] == UNKNOWN_VALUE)
							os.print("?");
						else
						{
							int vals = valueCount(j);
							if (vals == 0) os.print(row[j]);
							else
							{
								int val = (int)row[j];
								if (val >= vals) throw new IllegalArgumentException("Value out of range.");
								os.print(attrValue(j, val));
							}
						}
						
						if (j + 1 < cols())	os.print(",");
					}
					os.println();
				}
			}
			catch (FileNotFoundException e)
			{
				throw new IllegalArgumentException("Error creating file: " + filename + ".");
			}
			finally
			{
				os.close();
			}
		}

		/// Makes a rows-by-columns matrix of *ALL CONTINUOUS VALUES*.
		/// This method wipes out any data currently in the matrix. It also
		/// wipes out any meta-data.
		public void setSize(int rows, int cols)
		{
			m_data.clear();

			// Set the meta-data
			m_filename = "";
			m_attr_name.clear();
			m_str_to_enum.clear();
			m_enum_to_str.clear();

			// Make space for each of the columns, then each of the rows
			newColumns(cols);
			newRows(rows);
		}

		/// Clears this matrix and copies the meta-data from that matrix.
		/// In other words, it makes a zero-row matrix with the same number
		/// of columns as "that" matrix. You will need to call newRow or newRows
		/// to give the matrix some rows.
		@SuppressWarnings("unchecked")
		public void copyMetaData(Matrix that)
		{
			m_data.clear();
			m_attr_name = new ArrayList<String>(that.m_attr_name);
			
			// Make a deep copy of that.m_str_to_enum
			m_str_to_enum = new ArrayList<HashMap<String, Integer>>();
			for (HashMap<String, Integer> map : that.m_str_to_enum)
			{
				HashMap<String, Integer> temp = new HashMap<String, Integer>();
				for (Map.Entry<String, Integer> entry : map.entrySet())
					temp.put(entry.getKey(), entry.getValue());
				
				m_str_to_enum.add(temp);
			}
			
			// Make a deep copy of that.m_enum_to_string
			m_enum_to_str = new ArrayList<HashMap<Integer, String>>();
			for (HashMap<Integer, String> map : that.m_enum_to_str)
			{
				HashMap<Integer, String> temp = new HashMap<Integer, String>();
				for (Map.Entry<Integer, String> entry : map.entrySet())
					temp.put(entry.getKey(), entry.getValue());
				
				m_enum_to_str.add(temp);
			}
		}

		/// Adds a column to this matrix with the specified number of values. (Use 0 for
		/// a continuous attribute.) This method also sets the number of rows to 0, so
		/// you will need to call newRow or newRows when you are done adding columns.
		public void newColumn(int vals)
		{
			m_data.clear();
			String name = "col_" + cols();
			
			m_attr_name.add(name);
			
			HashMap<String, Integer> temp_str_to_enum = new HashMap<String, Integer>();
			HashMap<Integer, String> temp_enum_to_str = new HashMap<Integer, String>();
			
			for (int i = 0; i < vals; i++)
			{
				String sVal = "val_" + i;
				temp_str_to_enum.put(sVal, i);
				temp_enum_to_str.put(i, sVal);
			}
			
			m_str_to_enum.add(temp_str_to_enum);
			m_enum_to_str.add(temp_enum_to_str);
		}
		
		/// Adds a column to this matrix with 0 values (continuous data).
		public void newColumn()
		{
			this.newColumn(0);
		}
		
		/// Adds n columns to this matrix, each with 0 values (continuous data).
		public void newColumns(int n)
		{
			for (int i = 0; i < n; i++)
				newColumn();
		}
		
		/// Adds one new row to this matrix. Returns a reference to the new row.
		public double[] newRow()
		{
			int c = cols();
			if (c == 0)
				throw new IllegalArgumentException("You must add some columns before you add any rows.");
			double[] newRow = new double[c];
			m_data.add(newRow);
			return newRow;
		}
		
		/// Adds 'n' new rows to this matrix
		public void newRows(int n)
		{
			for (int i = 0; i < n; i++)
				newRow();
		}
		
		/// Returns the number of rows in the matrix
		public int rows() { return m_data.size(); }
		
		/// Returns the number of columns (or attributes) in the matrix
		public int cols() { return m_attr_name.size(); }
		
		/// Returns the name of the specified attribute
		public String attrName(int col) { return m_attr_name.get(col); }
		
		/// Returns the name of the specified value
		public String attrValue(int attr, int val)
		{		
			String value = m_enum_to_str.get(attr).get(val);
			if (value == null)
				throw new IllegalArgumentException("No name.");
			else return value;
		}
		
		/// Returns a reference to the specified row
		public double[] row(int index) { return m_data.get(index); }
		
		/// Swaps the positions of the two specified rows
		public void swapRows(int a, int b)
		{
			double[] temp = m_data.get(a);
			m_data.set(a, m_data.get(b));
			m_data.set(b, temp);
		}
		
		/// Returns the number of values associated with the specified attribute (or column)
		/// 0 = continuous, 2 = binary, 3 = trinary, etc.
		public int valueCount(int attr) { return m_enum_to_str.get(attr).size(); }
		
		/// Returns the mean of the elements in the specified column. (Elements with the value UNKNOWN_VALUE are ignored.)
		public double columnMean(int col)
		{
			double sum = 0.0;
			int count = 0;
			for (double[] list : m_data)
			{
				double val = list[col];
				if (val != UNKNOWN_VALUE)
				{
					sum += val;
					count++;
				}
			}
			
			return sum / count;
		}
		
		/// Returns the minimum element in the specified column. (Elements with the value UNKNOWN_VALUE are ignored.)
		public double columnMin(int col)
		{
			double min = Double.MAX_VALUE;
			for (double[] list : m_data)
			{
				double val = list[col];
				if (val != UNKNOWN_VALUE)
					min = Math.min(min, val);
			}
			
			return min;
		}

		/// Returns the maximum element in the specifed column. (Elements with the value UNKNOWN_VALUE are ignored.)
		public double columnMax(int col)
		{
			double max = -Double.MAX_VALUE;
			for (double[] list : m_data)
			{
				double val = list[col];
				if (val != UNKNOWN_VALUE)
					max = Math.max(max, val);
			}
			
			return max;
		}
		
		/// Returns the most common value in the specified column. (Elements with the value UNKNOWN_VALUE are ignored.)
		public double mostCommonValue(int col)
		{
			HashMap<Double, Integer> counts = new HashMap<Double, Integer>();
			for (double[] list : m_data)
			{
				double val = list[col];
				if (val != UNKNOWN_VALUE)
				{
					Integer result = counts.get(val);
					if (result == null) result = 0;
					
					counts.put(val, result + 1);
				}
			}
			
			int valueCount = 0;
			double value   = 0;
			for (Map.Entry<Double, Integer> entry : counts.entrySet())
			{
				if (entry.getValue() > valueCount)
				{
					value      = entry.getKey();
					valueCount = entry.getValue();
				}
			}
			
			return value;
		}

		/// Copies the specified rectangular portion of that matrix, and puts it in the specified location in this matrix.
		public void copyBlock(int destRow, int destCol, Matrix that, int rowBegin, int colBegin, int rowCount, int colCount)
		{
			if (destRow + rowCount > this.rows() || destCol + colCount > this.cols())
				throw new IllegalArgumentException("Out of range for destination matrix.");
			if (rowBegin + rowCount > that.rows() || colBegin + colCount > that.cols())
				throw new IllegalArgumentException("Out of range for source matrix.");

			// Copy the specified region of meta-data
			for (int i = 0; i < colCount; i++)
			{
				m_attr_name.set(destCol + i, that.m_attr_name.get(colBegin + i));
				m_str_to_enum.set(destCol + i, new HashMap<String, Integer>(that.m_str_to_enum.get(colBegin + i)));
				m_enum_to_str.set(destCol + i, new HashMap<Integer, String>(that.m_enum_to_str.get(colBegin + i)));
			}

			// Copy the specified region of data
			for (int i = 0; i < rowCount; i++)
			{
				double[] source = that.row(rowBegin + i);
				double[] dest = this.row(destRow + i);
				for(int j = 0; j < colCount; j++)
					dest[j] = source[colBegin + j];
			}
		}
		
		/// Sets every element in the matrix to the specified value.
		public void setAll(double val)
		{
			for (double[] list : m_data) {
				for(int i = 0; i < list.length; i++)
					list[i] = val;
			}
		}

		/// Sets this to the identity matrix.
		public void setToIdentity()
		{
			setAll(0.0);
			int m = Math.min(cols(), rows());
			for(int i = 0; i < m; i++)
				row(i)[i] = 1.0;
		}

		/// Throws an exception if that has a different number of columns than
		/// this, or if one of its columns has a different number of values.
		public void checkCompatibility(Matrix that)
		{
			int c = cols();
			if (that.cols() != c)
				throw new IllegalArgumentException("Matrices have different number of columns.");
			
			for (int i = 0; i < c; i++)
			{
				if (valueCount(i) != that.valueCount(i))
					throw new IllegalArgumentException("Column " + i + " has mis-matching number of values.");
			}
		}
	}
	
	static public class LayerTanh {
		public Matrix weights; // rows are inputs, cols are outputs
		public double[] bias;
		public double[] net;
		public double[] activation;
		public double[] error;


		LayerTanh(int inputs, int outputs) {
			weights = new Matrix();
			weights.setSize(inputs, outputs);
			bias = new double[outputs];
			net = new double[outputs];
			activation = new double[outputs];
			error = new double[outputs];
		}


		LayerTanh(LayerTanh that) {
			weights = new Matrix(that.weights);
			bias = Vec.copy(that.bias);
			net = Vec.copy(that.net);
			activation = Vec.copy(that.activation);
			error = Vec.copy(that.error);
		}


		void copy(LayerTanh src) {
			if(src.weights.rows() != weights.rows() || src.weights.cols() != weights.cols())
				throw new IllegalArgumentException("mismatching sizes");
			weights.setSize(src.weights.rows(), src.weights.cols());
			weights.copyBlock(0, 0, src.weights, 0, 0, src.weights.rows(), src.weights.cols());
			for(int i = 0; i < bias.length; i++) {
				bias[i] = src.bias[i];
			}
		}


		int inputCount() { return weights.rows(); }
		int outputCount() { return weights.cols(); }


		void initWeights(Random r) {
			double dev = Math.max(0.3, 1.0 / weights.rows());
			for(int i = 0; i < weights.rows(); i++) {
				double[] row = weights.row(i);
				for(int j = 0; j < weights.cols(); j++) {
					row[j] = dev * r.nextGaussian();
				}
			}
			for(int j = 0; j < weights.cols(); j++) {
				bias[j] = dev * r.nextGaussian();
			}
		}


		int countWeights() {
			return weights.rows() * weights.cols() + bias.length;
		}


		int setWeights(double[] w, int start) {
			int oldStart = start;
			for(int i = 0; i < bias.length; i++)
				bias[i] = w[start++];
			for(int i = 0; i < weights.rows(); i++)
			{
				double[] row = weights.row(i);
				for(int j = 0; j < weights.cols(); j++)
					row[j] = w[start++];
			}
			return start - oldStart;
		}


		void feedForward(double[] in) {
			if(in.length != weights.rows())
				throw new IllegalArgumentException("size mismatch. " + Integer.toString(in.length) + " != " + Integer.toString(weights.rows()));
			for(int i = 0; i < net.length; i++)
				net[i] = bias[i];
			for(int j = 0; j < weights.rows(); j++) {
				double v = in[j];
				double[] w = weights.row(j);
				for(int i = 0; i < weights.cols(); i++)
					net[i] += v * w[i];
			}
		}


		void feedForward2(double[] in1, double[] in2) {
			if(in1.length + in2.length != weights.rows())
				throw new IllegalArgumentException("size mismatch. " + Integer.toString(in1.length) + " + " + Integer.toString(in2.length) + " != " + Integer.toString(weights.rows()));
			for(int i = 0; i < net.length; i++)
				net[i] = bias[i];
			for(int j = 0; j < in1.length; j++) {
				double v = in1[j];
				double[] w = weights.row(j);
				for(int i = 0; i < weights.cols(); i++)
					net[i] += v * w[i];
			}
			for(int j = 0; j < in2.length; j++) {
				double v = in2[j];
				double[] w = weights.row(in1.length + j);
				for(int i = 0; i < weights.cols(); i++)
					net[i] += v * w[i];
			}
		}


		void activate() {
			for(int i = 0; i < net.length; i++) {
				activation[i] = Math.tanh(net[i]);
			}
		}


		void computeError(double[] target) {
			if(target.length != activation.length)
				throw new IllegalArgumentException("size mismatch. " + Integer.toString(target.length) + " != " + Integer.toString(activation.length));
			for(int i = 0; i < activation.length; i++) {
				if(target[i] < -1.0 || target[i] > 1.0)
					throw new IllegalArgumentException("target value out of range for the tanh activation function");
				error[i] = target[i] - activation[i];
			}
		}


		void deactivate() {
			for(int i = 0; i < error.length; i++) {
				error[i] *= (1.0 - activation[i] * activation[i]);
			}
		}


		void feedBack(double[] upstream) {
			if(upstream.length != weights.rows())
				throw new IllegalArgumentException("size mismatch");
			for(int j = 0; j < weights.rows(); j++) {
				double[] w = weights.row(j);
				double d = 0.0;
				for(int i = 0; i < weights.cols(); i++) {
					d += error[i] * w[i];
				}
				upstream[j] = d;
			}
		}


		void refineInputs(double[] inputs, double learningRate) {
			if(inputs.length != weights.rows())
				throw new IllegalArgumentException("size mismatch");
			for(int j = 0; j < weights.rows(); j++) {
				double[] w = weights.row(j);
				double d = 0.0;
				for(int i = 0; i < weights.cols(); i++) {
					d += error[i] * w[i];
				}
				inputs[j] += learningRate * d;
			}
		}


		void updateWeights(double[] in, double learningRate) {
			for(int i = 0; i < bias.length; i++) {
				bias[i] += learningRate * error[i];
			}
			for(int j = 0; j < weights.rows(); j++) {
				double[] w = weights.row(j);
				double x = learningRate * in[j];
				for(int i = 0; i < weights.cols(); i++) {
					w[i] += x * error[i];
				}
			}
		}

		// Applies both L2 and L1 regularization to the weights and bias values
		void regularizeWeights(double lambda) {
			for(int i = 0; i < weights.rows(); i++) {
				double[] row = weights.row(i);
				for(int j = 0; j < row.length; j++) {
					row[j] *= (1.0 - lambda);
					if(row[j] < 0.0)
						row[j] += lambda;
					else
						row[j] -= lambda;
				}
			}
			for(int j = 0; j < bias.length; j++) {
				bias[j] *= (1.0 - lambda);
				if(bias[j] < 0.0)
					bias[j] += lambda;
				else
					bias[j] -= lambda;
			}
		}
	}

	static public class NeuralNet {
		public ArrayList<LayerTanh> layers;
		/// General-purpose constructor. (Starts with no layers. You must add at least one.)
		NeuralNet() {
			layers = new ArrayList<LayerTanh>();
		}


		/// Copy constructor
		NeuralNet(NeuralNet that) {
			layers = new ArrayList<LayerTanh>();
			for(int i = 0; i < that.layers.size(); i++) {
				layers.add(new LayerTanh(that.layers.get(i)));
			}
		}


		/// Initializes the weights and biases with small random values
		void init(Random r) {
			for(int i = 0; i < layers.size(); i++) {
				layers.get(i).initWeights(r);
			}
		}


		/// Copies all the weights and biases from "that" into "this".
		/// (Assumes the corresponding topologies already match.)
		void copy(NeuralNet that) {
			if(layers.size() != that.layers.size())
				throw new IllegalArgumentException("Unexpected number of layers");
			for(int i = 0; i < layers.size(); i++) {
				layers.get(i).copy(that.layers.get(i));
			}
		}


		/// Feeds "in" into this neural network and propagates it forward to compute predicted outputs.
		double[] forwardProp(double[] in) {
			LayerTanh l = null;
			for(int i = 0; i < layers.size(); i++) {
				l = layers.get(i);
				l.feedForward(in);
				l.activate();
				in = l.activation;
			}
			return l.activation;
		}


		/// Feeds the concatenation of "in1" and "in2" into this neural network and propagates it forward to compute predicted outputs.
		double[] forwardProp2(double[] in1, double[] in2) {
			LayerTanh l = layers.get(0);
			l.feedForward2(in1, in2);
			l.activate();
			double[] in = l.activation;
			for(int i = 1; i < layers.size(); i++) {
				l = layers.get(i);
				l.feedForward(in);
				l.activate();
				in = l.activation;
			}
			return l.activation;
		}


		/// Backpropagates the error to the upstream layer.
		void backProp(double[] target) {
			int i = layers.size() - 1;
			LayerTanh l = layers.get(i);
			l.computeError(target);
			l.deactivate();
			for(i--; i >= 0; i--) {
				LayerTanh upstream = layers.get(i);
				l.feedBack(upstream.error);
				upstream.deactivate();
				l = upstream;
			}
		}


		/// Backpropagates the error from another neural network. (This is used when training autoencoders.)
		void backPropFromDecoder(NeuralNet decoder) {
			int i = layers.size() - 1;
			LayerTanh l = decoder.layers.get(0);
			LayerTanh upstream = layers.get(i);
			l.feedBack(upstream.error);
			l = upstream;
			//l.bendHinge(learningRate);
			l.deactivate();
			for(i--; i >= 0; i--) {
				upstream = layers.get(i);
				l.feedBack(upstream.error);
				//upstream.bendHinge(learningRate);
				upstream.deactivate();
				l = upstream;
			}
		}


		/// Updates the weights and biases
		void descendGradient(double[] in, double learningRate) {
			for(int i = 0; i < layers.size(); i++) {
				LayerTanh l = layers.get(i);
				l.updateWeights(in, learningRate);
				in = l.activation;
			}
		}


		/// Keeps the weights and biases from getting too big
		void regularize(double learningRate, double lambda) {
			double amount = learningRate * lambda;
			double smallerAmount = 0.1 * amount;
			for(int i = 0; i < layers.size(); i++) {
				LayerTanh lay = layers.get(i);
				//lay.straightenHinge(amount);
				lay.regularizeWeights(smallerAmount);
			}
		}


		/// Refines the weights and biases with on iteration of stochastic gradient descent.
		void trainIncremental(double[] in, double[] target, double learningRate) {
			forwardProp(in);
			backProp(target);
			//backPropAndBendHinge(target, learningRate);
			descendGradient(in, learningRate);
		}


		/// Refines "in" with one iteration of stochastic gradient descent.
		void refineInputs(double[] in, double[] target, double learningRate) {
			forwardProp(in);
			backProp(target);
			layers.get(0).refineInputs(in, learningRate);
		}


		static void testMath() {
			NeuralNet nn = new NeuralNet();
			LayerTanh l1 = new LayerTanh(2, 3);
			l1.weights.row(0)[0] = 0.1;
			l1.weights.row(0)[1] = 0.0;
			l1.weights.row(0)[2] = 0.1;
			l1.weights.row(1)[0] = 0.1;
			l1.weights.row(1)[1] = 0.0;
			l1.weights.row(1)[2] = -0.1;
			l1.bias[0] = 0.1;
			l1.bias[1] = 0.1;
			l1.bias[2] = 0.0;
			nn.layers.add(l1);

			LayerTanh l2 = new LayerTanh(3, 2);
			l2.weights.row(0)[0] = 0.1;
			l2.weights.row(0)[1] = 0.1;
			l2.weights.row(1)[0] = 0.1;
			l2.weights.row(1)[1] = 0.3;
			l2.weights.row(2)[0] = 0.1;
			l2.weights.row(2)[1] = -0.1;
			l2.bias[0] = 0.1;
			l2.bias[1] = -0.2;
			nn.layers.add(l2);

			System.out.println("l1 weights:" + l1.weights.toString());
			System.out.println("l1 bias:" + Vec.toString(l1.bias));
			System.out.println("l2 weights:" + l2.weights.toString());
			System.out.println("l2 bias:" + Vec.toString(l2.bias));

			System.out.println("----Forward prop");
			double in[] = new double[2];
			in[0] = 0.3;
			in[1] = -0.2;
			double[] out = nn.forwardProp(in);
			System.out.println("activation:" + Vec.toString(out));

			System.out.println("----Back prop");
			double targ[] = new double[2];
			targ[0] = 0.1;
			targ[1] = 0.0;
			nn.backProp(targ);
			System.out.println("error 2:" + Vec.toString(l2.error));
			System.out.println("error 1:" + Vec.toString(l1.error));
			
			nn.descendGradient(in, 0.1);
			System.out.println("----Descending gradient");
			System.out.println("l1 weights:" + l1.weights.toString());
			System.out.println("l1 bias:" + Vec.toString(l1.bias));
			System.out.println("l2 weights:" + l2.weights.toString());
			System.out.println("l2 bias:" + Vec.toString(l2.bias));

			if(Math.abs(l1.weights.row(0)[0] - 0.10039573704287) > 0.0000000001)
				throw new IllegalArgumentException("failed");
			if(Math.abs(l1.weights.row(0)[1] - 0.0013373814241446) > 0.0000000001)
				throw new IllegalArgumentException("failed");
			if(Math.abs(l1.bias[1] - 0.10445793808048) > 0.0000000001)
				throw new IllegalArgumentException("failed");
			System.out.println("passed");
		}

		public static void testVisual() throws Exception {
			// Make some data
			Random rand = new Random(1234);
			Matrix features = new Matrix();
			features.setSize(1000, 2);
			Matrix labels = new Matrix();
			labels.setSize(1000, 2);
			for(int i = 0; i < 1000; i++) {
				
				double x = rand.nextDouble() * 2 - 1;
				double y = rand.nextDouble() * 2 - 1;
				features.row(i)[0] = x;
				features.row(i)[1] = y;
				labels.row(i)[0] = (y < x * x ? 0.9 : 0.1);
				labels.row(i)[1] = (x < y * y ? 0.1 : 0.9);
			}

			// Train on it
			NeuralNet nn = new NeuralNet();
			nn.layers.add(new LayerTanh(2, 30));
			nn.layers.add(new LayerTanh(30, 2));
			nn.init(rand);
			int iters = 10000000;
			double learningRate = 0.01;
			double lambda = 0.0001;
			for(int i = 0; i < iters; i++) {
				int index = rand.nextInt(features.rows());
				nn.regularize(learningRate, lambda);
				nn.trainIncremental(features.row(index), labels.row(index), 0.01);
				if(i % 1000000 == 0)
					System.out.println(Double.toString(((double)i * 100)/ iters) + "%");
			}

			// Visualize it
			for(int i = 0; i < nn.layers.size(); i++) {
				System.out.print("Layer " + Integer.toString(i) + ": ");
//				for(int j = 0; j < nn.layers.get(i).hinge.length; j++)
//					System.out.print(Double.toString(nn.layers.get(i).hinge[j]) + ", ");
				System.out.println();
			}
			BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
			double[] in = new double[2];
			for(int y = 0; y < 100; y++) {
				for(int x = 0; x < 100; x++) {
					in[0] = ((double)x) / 100 * 2 - 1;
					in[1] = ((double)y) / 100 * 2 - 1;
					double[] out = nn.forwardProp(in);
					int g = Math.max(0, Math.min(255, (int)(out[0] * 256)));
					image.setRGB(x, y, new Color(g, g, g).getRGB());
					g = Math.max(0, Math.min(255, (int)(out[1] * 256)));
					image.setRGB(x, y + 100, new Color(g, g, g).getRGB());
				}
			}
			ImageIO.write(image, "png", new File("viz.png"));
		}
	}

	
	/* ------------------------------------------------------------*/
	/* ----------------------Genetic Algorithm Below---------------*/
	/* ------------------------------------------------------------*/
		static class Evolution
	{
	
		static double[] evolveWeights() throws IOException
		{
				
			// Create a random initial population
			Random r = new Random();
			Matrix population = new Matrix(100, 291);
			for(int i = 0; i < 100; i++)
			{
				double[] chromosome = population.row(i);
				for(int j = 0; j < chromosome.length; j++)
					chromosome[j] = 0.03 * r.nextGaussian();
			}
	
	
			
			int numEvolutions = 0;
			int maxEvolutions = 3000;
			
			
			while(numEvolutions < maxEvolutions){
			//Add mutation
			//int mutationCount = 0;
			int mutationRate = 200; //1/mutation rate to be mutated
			double mutationAverage = 0.3;
			for(int i = 0; i < 100; i++)
			{
				double[] chromosome = population.row(i);
				for(int j = 0; j < chromosome.length; j++)
					
					if(r.nextInt(mutationRate)==0){
						//Pick random chromosone
						int mut = r.nextInt(291);
						double gaus = r.nextGaussian();
						chromosome[mut]+= mutationAverage * gaus;
					}
			}
			//Done adding mutations
			
			//Natural Selection
			
			//Choose pair of chromosones
			int numTournaments = 5;
			int probToSurvive = 66;
			for(int x = 0; x < numTournaments; x++){
				int cNum1 = r.nextInt(100); //First chromosome num
				int cNum2 = r.nextInt(100); //Second chromosome num
				
				double [] chromoOne = population.row(cNum1);
				double [] chromoTwo = population.row(cNum2);
	
				//If they aren't the same chromosome, continue to do battle! Also check if they aren't a dead chromo
				//I'm assuming the chances of 80 being zero are near to none
				
				while(cNum1 == cNum2 || chromoOne[1]==0.0 || chromoTwo[1] ==0.0){
					cNum1 = r.nextInt(100);
					cNum2 = r.nextInt(100); 
					chromoTwo = population.row(cNum2);
					chromoOne = population.row(cNum1);
				}
	
					int winner = 0;
					try {
						winner = Controller.doBattleNoGui(new Winner2016a(), new WebbNathaniel(chromoTwo));
					} catch (Exception e) {
						e.printStackTrace();
					}
	
					if(winner == 1){
						for(int i = 0; i < chromoTwo.length; i++)
							population.row(cNum2)[i] = 0; //Kill the chromoOne
					}
					else if(winner == -1){
						System.out.println("Chromo two won.");
						System.out.println(Arrays.toString(chromoTwo));
						//for(int i = 0; i < chromoTwo.length; i++)
						//	population.row(cNum1)[i] = 0; //Kill the chromoTWo	
					}
					
					
			}//End Natural Selection for loop
			
			//Replenish the population!
			
			int numCandidates = 5;
			double difference = 0.05;
			for(int i = 0; i < 100; i++){
				
				//If its a dead chromo, make a baby!!
				if(population.row(i)[0] == 0.0){
					int parent1 = r.nextInt(100); //Pick first parent
					
					while(parent1==i || population.row(parent1)[0] ==0.0) //Make sure its not the same as the dead child
						parent1 = r.nextInt(100);
					
					int candidates[] = new int[numCandidates];
					int parent2 = 0;
					for(int x = 0; x < numCandidates; x++){
						parent2 = r.nextInt(100); //Pick second parent
						
						while(parent2 == parent1 || parent2 == i || population.row(parent2)[0] ==0.0) //Make sure its not the same as the dead child or the first parent
							parent2 = r.nextInt(100);
						candidates[x] = parent2;			
					}
					
					//Find whos the most similiar
					double[] dad = population.row(parent1);
					int bestMom = 0;
					double parentDifference = 5000000; //We have hugely different parents
					double testDifference;
					for(int x = 0; x < numCandidates; x++){
						double[] testMom = population.row(candidates[x]);
						testDifference = 0;
						
						for(int c = 0; c < dad.length; c++){
							testDifference+= Math.pow((dad[c] - testMom[c]), 2);
						}
						if(testDifference < parentDifference && testMom[0]!=0.0){
							parentDifference = testDifference;
							bestMom = candidates[x];
						}
							
					}//Done finding best parent
					
					//Lets mate!
					double[] mom = population.row(bestMom);
					for(int x = 0; x < dad.length; x++){
						int rand = r.nextInt(2);
						
						if(rand == 0){
							population.row(i)[x] = dad[x];
							
						}
						else{
							population.row(i)[x] = mom[x];
						}
					}
					
				}
			}
	
			numEvolutions++;
			System.out.println("Evolution number: " + numEvolutions);
		}//End of while
	
			return population.row(40);
		}
	}
		
	/* ------------------------------------------------------------*/
	/* ------------------------------------------------------------*/
}
