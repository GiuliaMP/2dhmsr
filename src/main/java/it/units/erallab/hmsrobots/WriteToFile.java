package it.units.erallab.hmsrobots;

import it.units.erallab.hmsrobots.behavior.BehaviorUtils;
import it.units.erallab.hmsrobots.core.geometry.Point2;
import it.units.erallab.hmsrobots.tasks.locomotion.Outcome;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteToFile {

    private static File check(File file) {
        String originalFileName = file.getPath();
        while (file.exists()) {
            String newName = null;
            Matcher mNum = Pattern.compile("\\((?<n>[0-9]+)\\)\\.\\w+$").matcher(file.getPath());
            if (mNum.find()) {
                int n = Integer.parseInt(mNum.group("n"));
                newName = new StringBuilder(file.getPath()).replace(mNum.start("n"), mNum.end("n"), Integer.toString(n + 1))
                        .toString();
            }
            Matcher mExtension = Pattern.compile("\\.\\w+$").matcher(file.getPath());
            if (newName == null && mExtension.find()) {
                newName = new StringBuilder(file.getPath()).replace(
                        mExtension.start(),
                        mExtension.end(),
                        ".(1)" + mExtension.group()
                ).toString();
            }
            if (newName == null) {
                newName = file.getPath() + ".newer";
            }
            file = new File(newName);
        }
        return file;
    }

    public static void toFile(File file, SortedMap<Double, Outcome.Observation> observationsHistory,
                              SortedMap<Double, List<Boolean>> flagsHistory) {
        List<String> lines = new ArrayList<>(); //history = new ArrayList<>()
        for (double flagTime : flagsHistory.keySet()) {
            for (double obsTime : observationsHistory.keySet()) {
                if (obsTime > flagTime) {
                    //Point2 centre = BehaviorUtils.center(observationsHistory.get(obsTime).voxelPolies().values());
                    String up = String.valueOf(flagsHistory.get(flagTime).get(2));
                    String down = String.valueOf(flagsHistory.get(flagTime).get(1));
                    String left = String.valueOf(flagsHistory.get(flagTime).get(0));
                    String right = String.valueOf(flagsHistory.get(flagTime).get(3));
                    String line = flagTime + ";" + up+ ";" + down+ ";" + left+ ";" + right;
                    
                    lines.add(line);
                    break;
                }
            }
        }
        file = check(file);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("time;up;down;left;right" + System.lineSeparator()); //time;centreX;centreY;heightY;up;down;left;right
            for (String line : lines) {
                fileWriter.write(line + System.lineSeparator());
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
