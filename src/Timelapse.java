import lejos.nxt.*;
import lejos.util.Stopwatch;
import lejos.util.Timer;
import lejos.util.TimerListener;

/* --------------------------
 * Projet NXT Timelapse
 * Ecrit par Merlin Nimier-David
 * License Creative Commons
 *
 * CONFIGURATION DES MOTEURS:
 * A: déclancheur
 * B: roues
 * C: roue de sélection
 */

public class Timelapse implements TimerListener
{
	//PARAMETRES DU TIMELAPSE
	private int m_mode; //1 = photo / 2 = video
	private int m_features; //1 = still / 2 = pan / 3 = rotate / 4 = pan+rotate
	
	private float m_distanceTotale; //Distance à parcourir en cm
	private float m_angle; //Angle à tourner en degrés

	private int m_interval; //Temps entre chaque photo en s
	private double m_dureeTotale; //Durée totale du shooting en s

	//*****************
	//* VARIABLES
	//INTERFACE UTILISATUER
	private UI ui;
	//DECLANCHEUR
	private Stopwatch chrono; //Sert à garder trace du temps écoulé
	private Timer intervalometer; //Notre timer principal qui déclanchera les prises de vues
	private int cheeseDown; //Temps que met le doigt à descendre
	private int cheeseUp; //Temps que met le doigt à remonter
	//PAN
	private int panningTime; //Temps que met le robot à se déplacer à chaque interval
	private float distanceParInterval; //Distance à parcourir à chaque interval
	private float speed; //(calculée)La vitesse du moteur qui tient les roues, en degrés/s
	//*
	//*****************


	//MAIN
	public static void main(String[] args) throws Exception
	{
	    Timelapse lapse = new Timelapse();
	}

	public Timelapse()
	{ //CONSTRUCTEUR
	    //Initialisation de l'interface utilisateur
	    ui = new UI();
	    ui.ecranTitre();
	    ui.setup();

	    //m_mode = ui.getMode();

	    //Initialisation des paramètres
	    //A REMPLACER PAR UN USER INPUT
	    m_mode = ui.getMode();
	    m_features = ui.getFeature();
	    m_distanceTotale = ui.getDistance();
	    m_angle = ui.getAngle();
	    m_interval = ui.getInterval();
	    m_dureeTotale = ui.getDuree();
	    //Constantes
	    cheeseDown = 500;
	    cheeseUp = 300;
	    //PAN (calculées plus tard)
	    panningTime = 0;
	    distanceParInterval = 0;
	    speed = 0;

	    
	    //Initialisation des variables
	    chrono = new Stopwatch();
	    intervalometer = new Timer(m_interval*1000, this); //Le timer appelle la fonction timedOut de cet objet

	    //On continue (écran titre, start, etc)
	    try
	    {
		init();
	    }
	    catch(Exception error)
	    {
		System.out.println("Erreur d'endormissement");
	    }
	}

	private void init() throws Exception
	{ //Init: s'occupe de l'initialisation des paramètres et des variables

	    //On résume les paramètres choisis
	    ui.sumUp();
	    //Puis on est prêts à partir :)
	    ui.pressToStart();

	    //On commence !
	    start();
	    System.out.println("Timelapse en cours...");

	    LCD.clear();

	    //Boucle de run
	    while(chrono.elapsed() < (int)(m_dureeTotale*1000) && !Button.ESCAPE.isPressed())
	    { //Tant que la durée totale n'est pas atteinte
		//BARRE DE PROGRESSION
		ui.refreshProgressBar(chrono.elapsed(), m_dureeTotale);
	    }

	    //On est sortis de la boucle, on arrete tout
	    stop();
	    
	    //A REMPLACER PAR UN PROCESSUS D'ANNULATION USER-FRIENDLY
	    System.out.println("We're done !");
	    System.out.println(" ");
	    System.out.println(" ");
	    System.out.println("Press to quit");
	    Button.waitForPress();
	}

	private void start()
	{ //start : déclanche le chronomètre et met en place le timer
	    //APPLICATION DES FEATURES
	    switch(m_features)
	    {
		case 1: //1 : still
		    //On bloque les roues
		    Motor.B.stop();
		break;
		case 2: //2 : pan
		    //Calcul de la distance à parcourir entre chaque cliché
		    distanceParInterval = (float)((m_interval / m_dureeTotale) * m_distanceTotale); //En cm
		    //Temps disponnible pour le déplacement (incluant 1s de sécurité)
		    if(m_mode != 2) //Seulement si le mode n'est pas video
			panningTime = ((int)(m_interval*1000) - (cheeseUp+cheeseDown) - 1000)/1000; //En s
		    else
			panningTime = m_interval; //En s

		    //V = d/t
		    float vitesse = distanceParInterval / panningTime; //En cm/s
		    speed = (float)(vitesse / 0.033); //Conversion en degrés/s. 0.037 parcoure trop peu de distance.

		    Motor.B.setSpeed((int)speed); //On assigne la vitesse calculée
		break;

		case 3: //3 : rotate
		    System.out.println("Erreur : mode non implémenté");
		break;

		case 4: //4 : pan+rotate
		    System.out.println("Erreur : mode non implémenté");
		break;

		default:
		    System.out.println("Erreur : aucune feature choisie");
	    }

	    //Calcul de l'interval du déclancheur
	    int intervalTimer;
	    if(m_mode != 2) //Seulement si le mode n'est pas video
		intervalTimer = (int)((m_interval*1000) - (cheeseUp+cheeseDown) - (panningTime*1000)); //On calcule l'interval en ms, on soustrait les ms d'endormissement
	    else
		intervalTimer = 50; //On calcule l'interval en ms, on soustrait les ms d'endormissement
	    
	    intervalometer.setDelay(intervalTimer);

	    //On lance finalement le tout
	    chrono.reset();
	    intervalometer.start();
	}

	public void stop()
	{ //Stop : à la fin du timelapse, on remet tout à 0, on arrete le chrono et l'intervalomètre
	    LCD.clear();
	    Motor.A.stop();
	    Motor.B.stop();
	    chrono.reset();
	    intervalometer.stop();
	}

	public void timedOut()
	{ //timedOut : appelée par l'intervalomètre
	    try
	    {
		//On lance le panning si nécessaire
		if(m_features == 2 || m_features == 4)
		{
		    pan();
		}
		//Puis on lance le déclancheur
		if(m_mode != 2) //Seulement si le mode n'est pas video
		    cheese();
	    }
	    catch(Exception error)
	    {
		System.out.println("Erreur lors de l'appel du déclancheur");
	    }
	}

	public void cheese() throws Exception
	{ //cheese : prend la photo
	    Motor.A.setSpeed(80);
	    Motor.A.backward();
	    
	    //On fait poirauter le programme 1000ms
	    Thread.sleep(cheeseDown);
	    Motor.A.forward();
	    Thread.sleep(cheeseUp);
	    Motor.A.stop();
	    //LA PHOTO EST PRISE
	}

	public void pan() throws Exception
	{
	    Motor.B.forward();
	    Thread.sleep((int)(panningTime * 1000));
	    Motor.B.stop();
	}


	public void patiente(int delai) //delai en ms
	{ //Patiente : coince le programme dans une boucle tant que le delai n'est pas écoulé
	    int tZero = chrono.elapsed(); //Instant initial
	    System.out.println(tZero);
	    int deltaT;
	    do
	    {
		LCD.clear();
		deltaT = chrono.elapsed() - tZero;
		System.out.println(deltaT);
	    }
	    while(deltaT < delai); //On reste dans cette boucle tant qu'il ne s'est pas écoulé 1000ms
	}
}
