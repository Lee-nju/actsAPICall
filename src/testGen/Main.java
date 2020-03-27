package testGen;

import edu.uta.cse.fireeye.common.*;
import edu.uta.cse.fireeye.service.engine.*;
import edu.uta.cse.fireeye.util.Util;
import edu.uta.cse.fireeye.common.TestGenProfile.Algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) throws Exception {
        // 输入输出文件路径
        String inputFileName = "./model.txt";
        String outputFileName = "./output.txt";

        // 用于生成的对象
        // ActsConsoleManager model = new ActsConsoleManager();

        // 设置成默认的生成配置
        TestGenProfile profile = TestGenProfile.instance();
        profile.setCheckCoverage("on");
        // 混合力度
        profile.setDOI(-1);
        // 3-way
        // profile.setDOI(-1);

        // 通过映射调用类的私有函数
//        try {
//            Method method = ActsConsoleManager.class.getDeclaredMethod("setCommandLineProperties", TestGenProfile.class);
//            method.setAccessible(true);
//            method.invoke(model, profile);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 获取模型
        SUTInfoReader reader = new SUTInfoReader(inputFileName);
        SUT sut;
        try {
            sut = reader.getSUT();
        } catch (IOException var10) {
            Util.abort(var10.getMessage());
            return;
        }

        // 是否为一般的混合力度，t==1
        boolean flag;
        // 是否导出为文件
        boolean export;
        // 默认的混合力度为2
        int strength = 2;

        // 生成测试用例
        if (sut != null) {
            TestSet ts = null;
            // 是否为一般的混合力度，t==1
            flag = true;
            // 不为一般的混合力度，t==k
            // flag = false;
            // t = 3;
            if (flag) {
                // 存生成的测试用例集
                ts = FireEye.generateTestSet(ts, sut);
            } else {
                ArrayList<Relation> relationsCopy = sut.getRelationManager().getRelations();
                ArrayList<Relation> relationsInUse = new ArrayList();
                Relation e;

                relationsInUse = new ArrayList(relationsCopy);
                HashSet<Parameter> allParam = new HashSet();
                Iterator var8 = relationsInUse.iterator();

                while (var8.hasNext()) {
                    Relation r = (Relation) var8.next();
                    allParam.addAll(r.getParams());
                }

                if (allParam.size() < sut.getParams().size()) {
                    System.out.println("This system contains " + sut.getRelationManager().getRelations().size() + " relation(s)." + "\nBut there are still " + (sut.getParams().size() - allParam.size()) + " parameter(s) lack of relation." + "\nACTS will add default " + strength + "-way relation for all params to build mixed strength." + "\nAfter generation, ACTS will set back the original relations.");
                    relationsInUse.add(new Relation(strength, sut.getParams()));
                }

                if (relationsInUse.size() == 0) {
                    e = new Relation(2, sut.getParams());
                    relationsInUse.add(e);
                }

                sut.getRelationManager().setRelations(relationsInUse);
                long start = System.currentTimeMillis();
                Builder builder;
                if (profile.getAlgorithm() == Algorithm.ipof) {
                    builder = new Builder(sut);
                    ts = builder.generate(Algorithm.ipof);
                } else if (profile.getAlgorithm() == Algorithm.ipog_r) {
                    builder = new Builder(sut);
                    ts = builder.generate(Algorithm.ipog_r);
                } else if (profile.getAlgorithm() == Algorithm.ipof2) {
                    ForbesEngine forbes = new ForbesEngine(sut, 8);
                    forbes.build();
                    ts = forbes.getTestSet();
                } else if (profile.getAlgorithm() == Algorithm.paintball) {
                    Paintball pb = new Paintball(sut, profile.getDOI(), profile.getMaxTries());
                    ts = pb.build();
                } else if (profile.getAlgorithm() == Algorithm.bush) {
                    Bush bush = new Bush(sut);
                    bush.build();
                    ts = new TestSet(sut.getParams(), sut.getOutputParameters());
                    ts.addMatrix(bush.getMatrix());
                } else if (profile.getAlgorithm() == Algorithm.ipog_d) {
                    BinaryBuilder builder_ = new BinaryBuilder(sut.getParams(), sut.getOutputParameters());
                    ts = builder_.getTestSet(sut.getOutputParameters());
                } else if (profile.getAlgorithm() == Algorithm.basechoice) {
                    BaseChoice bc = new BaseChoice(sut);
                    ts = bc.build();
                } else if (profile.getAlgorithm() == Algorithm.nil) {
                    ts = sut.getExistingTestSet();
                    if (ts == null) {
                        ts = new TestSet();
                    }
                } else if (profile.getAlgorithm() == Algorithm.ipog) {
                    builder = new Builder(sut);
                    ts = builder.generate(Algorithm.ipog);
                } else {
                    System.err.println("Unknown algorithm: " + profile.getAlgorithm());
                }

                if (ts != null) {
                    float duration = (float) (System.currentTimeMillis() - start) / 1000.0F;
                    sut.getRelationManager().setRelations(relationsCopy);
                    System.out.println("Number of Tests\t: " + ts.getNumOfTests());
                    System.out.println("Time (seconds)\t: " + duration + " ");
                    ts.setGenerationTime(duration);
                    if (profile.checkCoverage()) {
                        System.out.println("\nCoverage Check:");
                        CoverageChecker checker = new CoverageChecker(ts, sut, profile.getDOI());
                        if (checker.check()) {
                            System.out.println("Coverage has been verified!");
                        } else {
                            System.out.println("Failed to verify coverage!");
                        }
                    }

                    System.out.println();
                }
            }


            // 覆盖率对象
            CoverageChecker checker = new CoverageChecker(ts, sut, profile.getDOI());

            // 输出1,2,3,...条子测试用例的覆盖率
            for (float r : checker.getCoverageRatios()) {
                System.out.println(r);
            }

            // 输出每条测试用例
            for (int[] t : ts.getMatrix()) {
                for (int k : t) {
                    System.out.print(k);
                }
                System.out.println();
            }

            // 不导出输出文件
            export = false;
            if (export) {
                // 根据输出格式，输出测试用例集
                TestSetWrapper wrapper = new TestSetWrapper(ts, sut);
                if (TestGenProfile.instance().getOutputFormat() == TestGenProfile.OutputFormat.numeric) {
                    wrapper.outputInNumericFormat(outputFileName);
                    System.out.println("Output numeric: " + wrapper);
                } else if (TestGenProfile.instance().getOutputFormat() == TestGenProfile.OutputFormat.nist) {
                    wrapper.outputInNistFormat(outputFileName);
                } else if (TestGenProfile.instance().getOutputFormat() == TestGenProfile.OutputFormat.csv) {
                    wrapper.outputInCSVFormat(outputFileName);
                } else if (TestGenProfile.instance().getOutputFormat() == TestGenProfile.OutputFormat.excel) {
                    wrapper.outputInExcelFormat(outputFileName);
                }
            }
        }
    }
}
