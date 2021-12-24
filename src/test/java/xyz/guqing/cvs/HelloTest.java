package xyz.guqing.cvs;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import com.github.difflib.unifieddiff.UnifiedDiff;
import com.github.difflib.unifieddiff.UnifiedDiffFile;
import com.github.difflib.unifieddiff.UnifiedDiffWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import xyz.guqing.cvs.utils.PatchUtils;

/**
 * @author guqing
 * @since 2021-12-21
 */
public class HelloTest {

    @Test
    public void test() {
        DiffRowGenerator generator = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .inlineDiffByWord(true)
            .build();
        List<DiffRow> rows = generator.generateDiffRows(
            Arrays.asList("This is a test senctence.", "This is the second line.",
                "And here is the finish."),
            Arrays.asList("This is a test for diffutils.", "This is the second line."));

        System.out.println("original | new <br/>");
        for (DiffRow row : rows) {
            System.out.println(row.getOldLine() + "|" + row.getNewLine() + "<br/>");
        }
    }

    @Test
    public void test0() {
        final String s = PatchUtils.diffToPatchString("", "hello this is a line.");
        System.out.println(s);
        // 先还原出v1的实际值
        final String v1 = PatchUtils.restoreContent(s, "");
        // 在与v1 diff 为v2
        final String v2 = PatchUtils.diffToPatchString(v1, "hello this is second line.");
        System.out.println(v2);

        // 恢复v2
        final String s1 = PatchUtils.restoreContent(v2, v1);
        System.out.println(s1);
    }

    @Test
    public void test2() throws IOException {
        List<String> original = new ArrayList<>();
        List<String> revised = new ArrayList<>();

        revised.add("line1");
        revised.add("line2");

        Patch<String> patch = DiffUtils.diff(original, revised);
        UnifiedDiff diff =
            UnifiedDiff.from("head", "tail", UnifiedDiffFile.from(null, "revised", patch));

        StringWriter writer = new StringWriter();
        UnifiedDiffWriter.write(diff, f -> original, writer, 5);
        System.out.println(writer);

        // String[] lines = writer.toString().split("\\n");
    }

    @Test
    public void test3() {
        DiffRowGenerator generator = DiffRowGenerator.create()
            .showInlineDiffs(true)
            .inlineDiffByWord(true)
            .build();
        List<DiffRow> rows = generator.generateDiffRows(
            Arrays.asList("This is a test senctence.", "This is the second line.",
                "And here is the finish."),
            Arrays.asList("This is a test for diffutils.", "This is the second line.",
                "this is a new line.", "这是一行新的啊."));
    }
}
