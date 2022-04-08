package it.units.erallab.hmsrobots.core.interactive;

import it.units.erallab.hmsrobots.behavior.BehaviorUtils;
import it.units.erallab.hmsrobots.core.geometry.Point2;
import it.units.erallab.hmsrobots.tasks.locomotion.Outcome;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        List<String> lines = new ArrayList<>();
        for (double flagTime : flagsHistory.keySet()) {
            Point2 center = BehaviorUtils.center(observationsHistory.get(flagTime).voxelPolies().values().stream().filter(Objects::nonNull).toList());
            String line = String.format("%.3f;%s,%s;%s;%s%.2f;%.2f;%.2f",
                    flagTime,
                    flagsHistory.get(flagTime).get(2),
                    flagsHistory.get(flagTime).get(1),
                    flagsHistory.get(flagTime).get(0),
                    flagsHistory.get(flagTime).get(3),
                    center.x(),
                    center.y(),
                    observationsHistory.get(flagTime).terrainHeight()
            );
            lines.add(line);
        }
        file = check(file);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("time;up;down;left;right;centreX;centreY;heightY" + System.lineSeparator());
            for (String line : lines) {
                fileWriter.write(line + System.lineSeparator());
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
