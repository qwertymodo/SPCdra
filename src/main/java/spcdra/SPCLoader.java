/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spcdra;

import java.io.IOException;
import java.util.*;

import ghidra.app.util.MemoryBlockUtils;
import ghidra.app.util.Option;
import ghidra.app.util.OptionUtils;
import ghidra.app.util.bin.ByteProvider;
import ghidra.app.util.importer.MessageLog;
import ghidra.app.util.opinion.AbstractLibrarySupportLoader;
import ghidra.app.util.opinion.LoadSpec;
import ghidra.framework.model.DomainObject;
import ghidra.program.model.address.AddressOutOfBoundsException;
import ghidra.program.model.address.AddressOverflowException;
import ghidra.program.model.lang.LanguageCompilerSpecPair;
import ghidra.program.model.listing.Program;
import ghidra.program.model.symbol.SourceType;
import ghidra.util.exception.CancelledException;
import ghidra.util.exception.InvalidInputException;
import ghidra.util.task.TaskMonitor;

import static ghidra.app.util.MemoryBlockUtils.createInitializedBlock;

/**
 * TODO: Provide class-level documentation that describes what this loader does.
 */
public class SPCLoader extends AbstractLibrarySupportLoader {
	private static final String OPT_EP_ADDRESS = "Entrypoint Address";

	@Override
	public String getName() {

		// TODO: Name the loader.  This name must match the name of the loader in the .opinion 
		// files.

		return "SPC700";
	}

	@Override
	public Collection<LoadSpec> findSupportedLoadSpecs(ByteProvider provider) throws IOException {
		List<LoadSpec> loadSpecs = new ArrayList<>();

		// TODO: Examine the bytes in 'provider' to determine if this loader can load it.  If it 
		// can load it, return the appropriate load specifications.
		
		if(new String(provider.getInputStream(0).readNBytes(33),0,33).compareTo("SNES-SPC700 Sound File Data v0.30") != 0)
			return loadSpecs;
		
		loadSpecs.add(new LoadSpec(this, 0, new LanguageCompilerSpecPair("spc700:LE:16:default", "default"), true));
		return loadSpecs;
	}

	@Override
	protected void load(ByteProvider provider, LoadSpec loadSpec, List<Option> options,
			Program program, TaskMonitor monitor, MessageLog log)
			throws CancelledException, IOException {
		
		var as = program.getAddressFactory().getDefaultAddressSpace();
		var spc = MemoryBlockUtils.createFileBytes(program, provider, monitor);

		// TODO: Load the bytes from 'provider' into the 'program'.
		try {
			createInitializedBlock(program, false, "ram", as.getAddress(0x0000), spc, 0x100, 0x10000, "", "", true, true, true, log);
			var st = program.getSymbolTable();
			st.createLabel(as.getAddress(0xF0), "TEST_REGISTER", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xF1), "CONTROL_REGISTER", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xF2), "DSP_ADDRESS_REGISTER", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xF3), "DSP_DATA_REGISTER", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xF4), "PORT0", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xF5), "PORT1", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xF6), "PORT2", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xF7), "PORT3", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xFA), "TIMER0", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xFB), "TIMER1", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xFC), "TIMER3", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xFD), "COUNTER0", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xFE), "COUNTER1", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xFF), "COUNTER3", SourceType.IMPORTED);
			st.createLabel(as.getAddress(0xFFC0), "IPL", SourceType.IMPORTED);
			
			var op = OptionUtils.getOption(OPT_EP_ADDRESS, options, "0");
			int ep = Integer.parseInt(op, 16);
			st.addExternalEntryPoint(as.getAddress(ep));
		} catch (AddressOverflowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AddressOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<Option> getDefaultOptions(ByteProvider provider, LoadSpec loadSpec,
			DomainObject domainObject, boolean isLoadIntoProgram) {
		List<Option> list =
			super.getDefaultOptions(provider, loadSpec, domainObject, isLoadIntoProgram);
		
		int ep = -1;
		try {
			var pc = provider.getInputStream(0x25).readNBytes(2);
			ep = (int)(pc[0] & 0xFF) + (int)(pc[1] << 8);
			list.add(new Option(OPT_EP_ADDRESS, String.format("%04X", ep)));
		} catch (IOException e) {
			list.add(new Option(OPT_EP_ADDRESS, ""));
			e.printStackTrace();
		}

		return list;
	}

	@Override
	public String validateOptions(ByteProvider provider, LoadSpec loadSpec, List<Option> options, Program program) {

		// TODO: If this loader has custom options, validate them here.  Not all options require
		// validation.

		return super.validateOptions(provider, loadSpec, options, program);
	}
}
