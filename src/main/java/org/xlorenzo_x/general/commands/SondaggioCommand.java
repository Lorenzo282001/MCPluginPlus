package org.xlorenzo_x.general.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.xlorenzo_x.Utility.MCPlugin_Util;
import org.xlorenzo_x.Utility.Sondaggio;
import org.xlorenzo_x.start.Main;

import java.util.*;

public class SondaggioCommand implements CommandExecutor, Listener {

    private static SondaggioCommand instance;
    private static Map<Player, Map<Sondaggio, List<String>>> sondaggiAttivi = new HashMap<>();
    private static Map<String, Integer> choice_points = new HashMap<>();
    private static List<String> risposteUtenti = new ArrayList<>();

    private static int secondiSondaggio = 60; // Timer di 60 secondi per il sondaggio! [DEFAULT]

    private static boolean isTimerActive = false;

    private SondaggioCommand () {}

    public static SondaggioCommand getInstance() {
        if (instance == null)
        {
            instance = new SondaggioCommand();
        }
        return instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {
        // Attivo il comando solo se in config viene impostato TRUE
        if (Boolean.parseBoolean(Main.getInstance().getConfig().getString("settings.telechat-command")))
        {
            if (cmd.getName().equalsIgnoreCase("telechat")) {
                if (!isTimerActive) {  // Se il timer non è attivo, posso creare un nuovo sondaggio!
                    if (sender instanceof Player p) {
                        if (args.length >= 3) { // Verifica che ci siano almeno 3 argomenti
                            int index_StartScelte = 0; // Indico il punto in cui c'è -scelte

                            boolean isLastANumber = false;
                            try {

                                // Se l'ultimo argomento e` un SOLO numero, cambio i secondi!
                                secondiSondaggio = Integer.parseInt(args[args.length -1]);
                                isLastANumber = true;

                            } catch (NumberFormatException e) {
                                System.out.println(Arrays.toString(e.getStackTrace()));
                            }

                            StringBuilder domandaSondaggioBuilder = new StringBuilder();
                            for (int i = 0; i < args.length; i++) { // Inizia da 1 per saltare "-scelte"

                                if (args[i].equalsIgnoreCase("-scelte")) {
                                    index_StartScelte = i;
                                    break;
                                }

                                domandaSondaggioBuilder.append(args[i]).append(" ");
                            }

                            if (index_StartScelte == 0 || index_StartScelte == args.length - 1) // Posizione non corretta o non è stato messo il paramentro -scelte
                            {
                                p.sendMessage(MCPlugin_Util.namePlugin + " Non hai inserito correttamente il parametro -scelte!");
                                return true;
                            }

                            String domandaSondaggio = domandaSondaggioBuilder.toString().trim(); // Rimuovi lo spazio finale

                            Bukkit.getServer().broadcastMessage(MCPlugin_Util.namePlugin + "Sondaggio iniziato da " + ChatColor.GOLD + p.getDisplayName());
                            Bukkit.getServer().broadcastMessage(MCPlugin_Util.namePlugin + " Domanda -> " + domandaSondaggio);
                            Bukkit.getServer().broadcastMessage(MCPlugin_Util.namePlugin + "Scelte: ");

                            // Ora possiamo gestire le scelte correttamente
                            int j = 1;
                            List<String> scelteDelSondaggio = new ArrayList<>();

                            if (!isLastANumber)
                            {
                                for (int i = index_StartScelte + 1; i <= args.length - 1; i++) { // Inizia da 1 per saltare "-scelte"
                                    Bukkit.getServer().broadcastMessage(MCPlugin_Util.namePlugin + " " + j + ") " + args[i]);
                                    scelteDelSondaggio.add(args[i]);
                                    j++;
                                }
                            }
                            else {
                                for (int i = index_StartScelte + 1; i <= args.length - 2; i++) { // Inizia da 1 per saltare "-scelte"
                                    Bukkit.getServer().broadcastMessage(MCPlugin_Util.namePlugin + " " + j + ") " + args[i]);
                                    scelteDelSondaggio.add(args[i]);
                                    j++;
                                }
                            }

                            Sondaggio createTelechat = new Sondaggio(domandaSondaggio, scelteDelSondaggio); // OK - Mi salvo il sondaggio
                            Map<Sondaggio, List<String>> sondaggioMappa = new HashMap<>();
                            sondaggioMappa.put(createTelechat, new ArrayList<>()); // -> Conterrà tutte le risposte del server [Entro 60 secondi!]

                            sondaggiAttivi.put(p, sondaggioMappa); // p è chi crea il sondaggio!
                            Bukkit.getServer().broadcastMessage(MCPlugin_Util.namePlugin + ChatColor.GOLD + " Sondaggio avviato. Tempo rimanete: "+ secondiSondaggio + " secondi!");
                            isTimerActive = true;
                            startTimer();
                        } else {
                            p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.GREEN + " Devi inserire almeno 3 parametri con '<Domanda sondaggio> -scelte  <Scelta_1> <Scelta_2> -TIMER(s)'");
                        }

                    } else {
                        sender.sendMessage(MCPlugin_Util.namePlugin + " Non sei un player!");
                        return true;
                    }
                }
                else { // Se mi trovo qui è perchè un sondaggio è già in corso! Aspettare la fine
                    if (sender instanceof Player p) {
                        p.sendMessage(MCPlugin_Util.namePlugin + ChatColor.DARK_RED + " Un sondaggio è già in corso. Aspetta la fine del sondaggio!");
                    }
                }
            }
        }
        else {
            sender.sendMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Comando disabilitato da config!");
        }

        return false;
    }

    // Inizio il sondaggio chat! -- Tengo d'occhio la variabile isTimerActive,
    // fino a quando è true la chat viene controllata dal onPlayerRepondToTeleChat Event!
    private void startTimer () {
       Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {

           isTimerActive = false;
           parsingRisposte(); // Parsifico le risposte quando il sondaggio è terminato!
           Bukkit.getServer().broadcastMessage(MCPlugin_Util.namePlugin + ChatColor.RED + " Il sondaggio è terminato!");

           // Check Winner
           checkWinnerSondaggio();

           // Reset
           sondaggiAttivi = new HashMap<>();
           risposteUtenti = new ArrayList<>();
           choice_points = new HashMap<>();

       }, 20L * secondiSondaggio);

    }

    @EventHandler
    public void onPlayerRepondToTeleChat(AsyncPlayerChatEvent event) {

        if (!isTimerActive)
            return; // Se il timer non è attivo, non controllare più la chat di gioco!

        risposteUtenti.add(event.getMessage()); // Per il tempo in secondi!

    }

    private void parsingRisposte () {

        // Capisco quali sono le risposte valide!
        for (Map.Entry<Player, Map<Sondaggio, List<String>>> entry : sondaggiAttivi.entrySet()) {
            // Chi ha fatto il sondaggio?
            Map<Sondaggio, List<String>> mappaInterna = entry.getValue();

            // Itera sulla mappa interna
            for (Map.Entry<Sondaggio, List<String>> innerEntry : mappaInterna.entrySet()) { // Siccome il sondaggio è solo uno, posso fare un break
                Sondaggio sondaggio = innerEntry.getKey();

                List<String> risposteDaInserire = innerEntry.getValue();

                // Inserisco le risposte degli utenti!

                for (String s : risposteUtenti) {
                    for (int z = 0; z < sondaggio.scelteSondaggio.size(); z++) {
                        if (s.equalsIgnoreCase(sondaggio.scelteSondaggio.get(z))) {
                            risposteDaInserire.add(s);
                        }
                    }
                }


                break;
            }
        }

        // Inserisco nella mappa choice_point, le risposte con associato il numero di punti ottenuti!
        for (Map.Entry<Player, Map<Sondaggio, List<String>>> entry : sondaggiAttivi.entrySet()) {
            // Chi ha fatto il sondaggio?
            Map<Sondaggio, List<String>> mappaInterna = entry.getValue();

            // Itera sulla mappa interna
            for (Map.Entry<Sondaggio, List<String>> innerEntry : mappaInterna.entrySet()) { // Siccome il sondaggio è solo uno, posso fare un break

                List<String> listaDelleRisposte = innerEntry.getValue(); // Qui ci sono tutte le risposte parsificate degli utenti chat!

                // Map: choice_points
                for (int x = 0; x < listaDelleRisposte.size(); x++) { // Faccio lo stesso ciclo per ogni

                    String current_choice = listaDelleRisposte.get(x);
                    int points = 0;
                    for (String s : listaDelleRisposte) {
                        if (current_choice.equals(s)) {
                            points++;
                        }
                    }

                    choice_points.put(current_choice, points);

                }

                break;
            }
        }

    }

    private void checkWinnerSondaggio () {

        // Inizializza il valore massimo e la scelta associata
        int maxValue = Integer.MIN_VALUE;
        String maxChoice = null;

        // Itera sulla mappa per trovare il valore massimo
        for (Map.Entry<String, Integer> entry : choice_points.entrySet()) {
            String choice = entry.getKey();
            int points = entry.getValue();

            // Verifica se il valore attuale è maggiore del massimo trovato finora
            if (points > maxValue) {
                maxValue = points;
                maxChoice = choice;
            }
        }

        if (maxChoice != null)
        {
            Bukkit.getServer().broadcastMessage(MCPlugin_Util.namePlugin + ChatColor.GREEN + " Il sondaggio dichiara vinto -> " + ChatColor.GOLD + maxChoice + ChatColor.GREEN + "  con " + maxValue + " punti!");
        }

    }


}
