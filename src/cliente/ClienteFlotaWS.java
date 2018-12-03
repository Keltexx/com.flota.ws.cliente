package cliente;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.*;

public class ClienteFlotaWS {

	// Sustituye esta clase por tu implementación.
	// Deberías copiar y modificar ligeramente la clase cliente que has implementado por ejemplo 
	// en la solución con sockets o RMI sin callbacks
	

	/**
	 * Implementa el juego 'Hundir la flota' mediante una interfaz gráfica (GUI)
	 */

	/** Parametros por defecto de una partida */
	public static final int NUMFILAS=8, NUMCOLUMNAS=8, NUMBARCOS=6;

	private GuiTablero guiTablero = null;			// El juego se encarga de crear y modificar la interfaz gráfica
	private GestorPartidas aux = null;                 // Objeto con los datos de la partida en juego
	
	/** Atributos de la partida guardados en el juego para simplificar su implementación */
	private int quedan = NUMBARCOS, disparos = 0;

	/**
	 * Programa principal. Crea y lanza un nuevo juego
	 * @param args
	 */
	public static void main(String[] args) {
		ClienteFlotaWS juego = new ClienteFlotaWS();
		juego.ejecuta();
	} // end main

	/**
	 * Lanza una nueva hebra que crea la primera partida y dibuja la interfaz grafica: tablero
	 */
	private void ejecuta() {
		// Instancia la primera partida
		try {
			aux = new GestorPartidas();
			aux.nuevaPartida(NUMFILAS, NUMCOLUMNAS, NUMBARCOS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				guiTablero = new GuiTablero(NUMFILAS, NUMCOLUMNAS);
				guiTablero.dibujaTablero();
			}
		});
	} // end ejecuta

	/******************************************************************************************/
	/*********************  CLASE INTERNA GuiTablero   ****************************************/
	/******************************************************************************************/
	private class GuiTablero {

		private int numFilas, numColumnas;

		private JFrame frame = null;        // Tablero de juego
		private JLabel estado = null;       // Texto en el panel de estado
		private JButton buttons[][] = null; // Botones asociados a las casillas de la partida

		/**
         * Constructor de una tablero dadas sus dimensiones
         */
		GuiTablero(int numFilas, int numColumnas) {
			this.numFilas = numFilas;
			this.numColumnas = numColumnas;
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		}

		/**
		 * Dibuja el tablero de juego y crea la partida inicial
		 */
		public void dibujaTablero() {
			anyadeMenu();
			anyadeGrid(numFilas, numColumnas);		
			anyadePanelEstado("Intentos: " + disparos + "    Barcos restantes: " + quedan);		
			frame.setSize(300, 300);
			frame.setVisible(true);	
		} // end dibujaTablero

		/**
		 * Anyade el menu de opciones del juego y le asocia un escuchador
		 */
		private void anyadeMenu() {
			//Creacion de opciones del menu
            JMenuBar menu = new JMenuBar();	
            JMenu opciones = new JMenu("Opciones");
            JMenuItem solucion = new JMenuItem("Mostrar solucion");
            JMenuItem nueva = new JMenuItem("Nueva partida");
            JMenuItem salir = new JMenuItem("Salir");
            
            //Llamada al escuchador con cada opcion del menu
            MenuListener escuchador = new MenuListener();
            solucion.addActionListener(escuchador);
            nueva.addActionListener(escuchador);
            salir.addActionListener(escuchador);
            
            //Anade las opciones a la ventana
            opciones.add(solucion);
            opciones.add(nueva);
            opciones.add(salir);
            menu.add(opciones);
            frame.add(menu, BorderLayout.NORTH);
		} // end anyadeMenu

		/**
		 * Anyade el panel con las casillas del mar y sus etiquetas.
		 * Cada casilla sera un boton con su correspondiente escuchador
		 * @param nf	numero de filas
		 * @param nc	numero de columnas
		 */
		private void anyadeGrid(int nf, int nc) {
			JPanel cuadr = new JPanel();
			GridLayout experimentLayout = new GridLayout(nf+1,nc+2);
			ButtonListener escuchador = new ButtonListener();
			JLabel label;
			buttons = new JButton[nf][nc];
			cuadr.setLayout(experimentLayout);
			
			for(int i=0; i<nf+1; i++){	//Recorre las filas de la matriz
				for(int j=0; j<nc+2; j++){	//Recorre las columnas de la matriz
					if((i==0 && j==0)||(i==0 && j==nc+1))	//Anade espacio en blanco en la primera y ultima casilla de la primera fila
						cuadr.add(label = new JLabel("   "));
					else if(i==0 && j >=1)	//Numera las columnas en la primera fila
						cuadr.add(label = new JLabel("   "+j));
					else if(j==0 || j==nc+1)	//Numera mediante letras las filas al principio y al final
						cuadr.add(label = new JLabel("   "+Character.toString((char)('A'+i-1))));
					else{
						JButton boton = new JButton();
						boton.putClientProperty("fila", i-1);
						boton.putClientProperty("columna", j-1);
						boton.addActionListener(escuchador);
						buttons[i-1][j-1] = boton;
						cuadr.add(boton);
					}
				}
				
			}
			frame.add(cuadr);
		} // end anyadeGrid


		/**
		 * Anyade el panel de estado al tablero
		 * @param cadena	cadena inicial del panel de estado
		 */
		private void anyadePanelEstado(String cadena) {	
			JPanel panelEstado = new JPanel();
			estado = new JLabel(cadena);
			panelEstado.add(estado);
			// El panel de estado queda en la posición SOUTH del frame
			frame.getContentPane().add(panelEstado, BorderLayout.SOUTH);
		} // end anyadePanel Estado

		/**
		 * Cambia la cadena mostrada en el panel de estado
		 * @param cadenaEstado	nuevo estado
		 */
		public void cambiaEstado(String cadenaEstado) {
			estado.setText(cadenaEstado);
		} // end cambiaEstado

		/**
		 * Muestra la solucion de la partida y marca la partida como finalizada
		 */
		public void muestraSolucion() throws IOException {
			quedan=0;
			for (int i = 0; i < numFilas; i++) {
				for (int j = 0; j < numColumnas; j++){
					int toque;
					toque = aux.pruebaCasilla(i, j);
					if(toque==-1) {
						guiTablero.pintaBoton(guiTablero.buttons[i][j],Color.cyan);
					}else {
						guiTablero.pintaBoton(guiTablero.buttons[i][j],Color.magenta);
					}
				}
			}
			
			
		} // end muestraSolucion


		/**
		 * Pinta un barco como hundido en el tablero
		 * @param cadenaBarco	cadena con los datos del barco codifificados como
		 *                      "filaInicial#columnaInicial#orientacion#tamanyo"
		 */
		public void pintaBarcoHundido(String cadenaBarco) {
			String[] parts = cadenaBarco.split("#");
			int filaInicial = Integer.parseInt(parts[0]);
			int columnaInicial = Integer.parseInt(parts[1]);     
			String orientacion = parts[2];     
			int tamanyo = Integer.parseInt(parts[3]);  

			if (orientacion.equals("V")){
				for (int i=filaInicial; i<tamanyo+filaInicial; i++)
					pintaBoton(buttons[i][columnaInicial], Color.red);
			}
			
			if (orientacion.equals("H")){
				for (int i=columnaInicial; i<tamanyo+columnaInicial; i++)
					pintaBoton(buttons[filaInicial][i], Color.red);
			}
			
		} // end pintaBarcoHundido

		/**
		 * Pinta un botón de un color dado
		 * @param b			boton a pintar
		 * @param color		color a usar
		 */
		public void pintaBoton(JButton b, Color color) {
			b.setBackground(color);
			// El siguiente código solo es necesario en Mac OS X
			b.setOpaque(true);
			b.setBorderPainted(false);
		} // end pintaBoton

		/**
		 * Limpia las casillas del tablero pintándolas del gris por defecto
		 */
		public void limpiaTablero() {
			for (int i = 0; i < numFilas; i++) {
				for (int j = 0; j < numColumnas; j++) {
					buttons[i][j].setBackground(null);
					buttons[i][j].setOpaque(true);
					buttons[i][j].setBorderPainted(true);
				}
			}
		} // end limpiaTablero

		/**
		 * 	Destruye y libera la memoria de todos los componentes del frame
		 */
		public void liberaRecursos() {
			frame.dispose();
		} // end liberaRecursos


	} // end class GuiTablero

	/******************************************************************************************/
	/*********************  CLASE INTERNA MenuListener ****************************************/
	/******************************************************************************************/

	/**
	 * Clase interna que escucha el menu de Opciones del tablero
	 * 
	 */
	private class MenuListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch(e.getActionCommand()){
			case "Salir": 
				guiTablero.liberaRecursos();
				break;
			case "Mostrar solucion":
				try {
					guiTablero.muestraSolucion();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				break;
			case "Nueva partida":
				guiTablero.limpiaTablero();
				try {
					aux.nuevaPartida(NUMFILAS, NUMCOLUMNAS, NUMBARCOS);
					quedan = NUMBARCOS; 
					disparos = 0;
					guiTablero.cambiaEstado("Intentos: " + disparos + "    Barcos restantes: " + quedan);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				break;				
			default:
				break;
			} //end switch
			
		} //end actionPerformed
	} // end class MenuListener



	/******************************************************************************************/
	/*********************  CLASE INTERNA ButtonListener **************************************/
	/******************************************************************************************/
	/**
	 * Clase interna que escucha cada uno de los botones del tablero
	 * Para poder identificar el boton que ha generado el evento se pueden usar las propiedades
	 * de los componentes, apoyandose en los metodos putClientProperty y getClientProperty
	 */
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int i=(int)((JButton)e.getSource()).getClientProperty("fila"), j=(int)((JButton)e.getSource()).getClientProperty("columna");
			if(quedan!=0){
				if(!guiTablero.buttons[i][j].getBackground().equals(Color.yellow) &&
						!guiTablero.buttons[i][j].getBackground().equals(Color.red)){
					int toque;
					toque = aux.pruebaCasilla(i,j);
					switch(toque){
					case -1:	//AGUA
						guiTablero.pintaBoton(guiTablero.buttons[i][j],Color.cyan);
						break;
					case -2:	//TOCADO
						guiTablero.pintaBoton(guiTablero.buttons[i][j],Color.yellow);
						break;
					default:	//HUNDIDO
						quedan--;
						guiTablero.pintaBarcoHundido(aux.getBarco(toque));
						break;
					} //end switch
					disparos++;
				}
				if(quedan==0)
					guiTablero.cambiaEstado("GAME OVER en " + disparos + " disparos");

				else
					guiTablero.cambiaEstado("Intentos: " + disparos + "    Barcos restantes: " + quedan);
			} //end if no quedan barcos
			
        } // end actionPerformed

	} // end class ButtonListener

	

	

}
