package de.graeuler.garden.testhelpers;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import de.graeuler.garden.data.DataRecord;

public class DataRecordBuilder {

	private final static String[] keyset100 = { "xEWTFFrReb", "GOawQgsBBq", "kdmNCxIPeD", "uNIZxsvTfD", "VKCGbXWVMt",
			"LneYRDzcIz", "ETVgydojxf", "mTtnZGPCXL", "ZXmpjGUpSS", "yaqxwnLCfF", "dspnUFlITa", "odroULtFYp",
			"QOPOaQyFpv", "TSkzEXVwGw", "GEdVddnSeB", "tOfTSuZBHk", "DngrmhTTZu", "DXEeWHFsoN", "cHRVScLgQQ",
			"IwnwEPbiIN", "MLjSlPmNWs", "vgZXeRgzsi", "vbrRGcnGHr", "ZyjSyqWqRp", "jIMNCRIVuA", "tgNHbPLBJI",
			"aQbKqOXqKg", "VMZNrbMdeg", "dkBFfnvUrP", "OnErvYVWdk", "AwnklUJTqd", "mLlveRuuBe", "RExSmmViyD",
			"OdDwqyMLdf", "LJtCASKZdK", "yxKAfbzrZF", "yTIHHKWbNQ", "bpWsEGUBqN", "itXhWRLSZh", "ApbQgDpHot",
			"MMmkcyEEwd", "mLfCMgPEyJ", "lePScNwsFB", "uolyBGBgDW", "SgvyXlgzPe", "kJcnyiLXsc", "UFLRqtepUR",
			"HmhKbnnRRq", "HxKautVqNj", "MWCrRiiCvN", "OSKOcBvLBW", "QvZXveIZah", "DNOKYqqgFD", "VTwOAsEyIK",
			"lkFAmwEyen", "VVYcbtzqFG", "iFQzIRQTTy", "YrtecGPjUG", "HhUSgrFYrj", "qGPwiuowDc", "ocRdIetvJz",
			"vTmzOyOciB", "DNpjwAvKYW", "pEEafovDlW", "BPdIjQmaZq", "btfQMwVkXM", "cNJgCFOgpf", "BstRnvtTRZ",
			"bEbBjFcjiu", "FUQrIKPgLo", "EXRhscBxIp", "dGKKTeNuZW", "NfeyIlnaTC", "bjYxEcOnAt", "MRwRmlFIay",
			"hLsNvEWoNS", "hReEmUvrzD", "WTzaIbCrjP", "TFzslrwOSQ", "UzsqpVMaFn", "qJBzHwatgu", "ENNVuZRQAu",
			"EvGutpjPaf", "laGcwTVLQL", "weiQAIjeAx", "TtPNWWfwTJ", "RheKAtvPxH", "rxDjfQNjOH", "XlVJQSlnib",
			"dVmBCiwenP", "LKKXYXWpJC", "UYZNhnZVdX", "UlXhxqubNR", "hheVrXVAod", "LoQJNwsSdD", "ppqobpJIQQ",
			"oVRlBQcJor", "JVTesqzFra", "jDjXwtQyic", "pscVCDflRV" };

	public static DataRecord randomDataRecord() {
		return randomDataRecord(100);
	}
	
	public static DataRecord randomDataRecord(int differentKeys) {
		if (differentKeys < 1 || differentKeys > 100) {
			differentKeys = 100;
		}
		ThreadLocalRandom random = ThreadLocalRandom.current();
		return new DataRecord(keyset100[random.nextInt(differentKeys)], new Double(random.nextDouble()));
	}

	public static Stream<DataRecord> stream(int loadSize) {
		return Stream.generate(() -> DataRecordBuilder.randomDataRecord()).limit(loadSize);
	}

	public static Stream<DataRecord> stream(int loadSize, int differentKeys) {
		return Stream.generate(() -> DataRecordBuilder.randomDataRecord(differentKeys)).limit(loadSize);
	}

}
