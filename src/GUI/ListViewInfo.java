/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import java.util.UUID;

/**
 *
 * @author sebastian
 */
public class ListViewInfo {

    private UUID id;
    private String name;

    public ListViewInfo(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public UUID getId() {
        return id;
    }
}
