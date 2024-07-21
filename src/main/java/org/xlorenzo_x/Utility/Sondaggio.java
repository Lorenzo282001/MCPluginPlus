package org.xlorenzo_x.Utility;

import java.util.List;

public class Sondaggio {

    public String domanda;
    public List<String> scelteSondaggio;

    public Sondaggio (String domanda_sondaggio, List<String> scelteSondaggio) {
        this.domanda = domanda_sondaggio;
        this.scelteSondaggio = scelteSondaggio;
    }

    public boolean contieneRisposta(String risposta) {
        for (String scelta : this.scelteSondaggio) {
            if (risposta.equalsIgnoreCase(scelta)) {
                return true;
            }
        }
        return false;
    }

    public String getDomanda() {
        return domanda;
    }

    public List<String> getScelteSondaggio() {
        return scelteSondaggio;
    }
}
