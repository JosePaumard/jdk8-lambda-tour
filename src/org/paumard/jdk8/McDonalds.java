/*
 * Copyright (C) 2014 José Paumard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.paumard.jdk8;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.paumard.model.McDonald;

/**
 *
 * @author José Paumard
 */
public class McDonalds {
    
    public static void main(String... args) throws Exception {
        
        Stream<String> lines = Files.lines(Paths.get("files", "mcdonalds.csv")) ;
        List<McDonald> mcdos = lines.map(s -> {
            // -149.95038,61.13712,"McDonalds-Anchorage,AK","3828 W Dimond Blvd, Anchorage,AK, (907) 248-0597"
            // -72.84817,41.27988,"McDonalds-Branford,CT","424 W Main St, Branford,CT, (203) 488-9353"
            String [] strings = s.split(",") ;
            McDonald mdo = new McDonald() ;
            mdo.setLatitude(Double.parseDouble(strings[0])) ;
            mdo.setLongitude(Double.parseDouble(strings[1])) ;
            mdo.setName(strings[2].substring(1) + strings[3].substring(0, strings[3].length() - 1)) ;
            mdo.setAddress(strings[4].substring(1)) ;
            mdo.setCity(strings[5].trim()) ;
            mdo.setState(strings[6].trim()) ;
            if (mdo.state().endsWith("\"")) {
                mdo.setState(mdo.state().substring(0, mdo.state().length() - 1)) ;
            }
            if (mdo.state().contains(" ")) {
                mdo.setState(mdo.state().substring(0, mdo.state().indexOf(" "))) ;
            }
            if (mdo.state().length() > 2) {
                mdo.setState(strings[7].trim()) ;
            }
            return mdo ;
        }).collect(Collectors.toList()) ;
        
        System.out.println("# of McDos = " + mcdos.size()) ;
        
        // The number of cities that have a McDonald
        long nTowns = 
        mcdos.stream()
                .map(McDonald::city)
                .collect(Collectors.toSet())
                .size() ;
        System.out.println("The number of cities that have a McDonald : " + nTowns) ;
        
        // The city has the most MacDonald
        Map.Entry<String, Long> entry = 
        mcdos.stream()
                .collect(
                        Collectors.groupingBy(
                                McDonald::city, 
                                Collectors.counting()
                        )
                )
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .get() ;
        System.out.println("The city has the most MacDonald : " + entry) ;
        
    }
}
