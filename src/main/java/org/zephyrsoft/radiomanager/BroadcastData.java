/**
 *
 */
package org.zephyrsoft.radiomanager;

/**
 * Contains the return data from a check for an available broadcast.
 */
public class BroadcastData {

	private CheckResultEnum resultType;
	private String resultText;

	public BroadcastData(CheckResultEnum resultType, String resultText) {
		this.resultType = resultType;
		this.resultText = resultText;
	}

	public CheckResultEnum getResultType() {
		return resultType;
	}

	public String getResultText() {
		return resultText;
	}

	public void setResultType(CheckResultEnum resultType) {
		this.resultType = resultType;
	}

	public void setResultText(String resultText) {
		this.resultText = resultText;
	}

}
