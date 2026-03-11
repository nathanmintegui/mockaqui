<script lang="ts">
	import logo from '$lib/assets/logo.webp';
	import type { PageProps } from './$types';
	import { Transaction } from '@codemirror/state';
	import { basicSetup, EditorView } from 'codemirror';
	import { json } from '@codemirror/lang-json';
	import { onMount, tick } from 'svelte';

	let { data }: PageProps = $props();

	const httpVerbs = [
		{ id: 1, label: 'GET', color: 'bg-green-300' },
		{ id: 2, label: 'POST', color: 'bg-red-200' },
		{ id: 3, label: 'PUT', color: 'bg-orange-200' },
		{ id: 4, label: 'PATCH', color: 'bg-lime-200' },
		{ id: 5, label: 'DELETE', color: 'bg-sky-200' }
	] as const;

	const statusCode = [
		{ id: 1, code: 200, label: 'OK', color: 'bg-green-300' },
		{ id: 2, code: 201, label: 'No Content', color: 'bg-red-200' },
		{ id: 3, code: 202, label: '', color: 'bg-orange-200' },
		{ id: 4, code: 204, label: '', color: 'bg-lime-200' },
		{ id: 5, code: 400, label: 'Bad Request', color: 'bg-sky-200' },
		{ id: 6, code: 401, label: '', color: 'bg-sky-200' },
		{ id: 7, code: 422, label: 'Unprocessable Entity', color: 'bg-sky-200' },
		{ id: 8, code: 500, label: 'Internal Server Error', color: 'bg-sky-200' }
	] as const;

	const responseLatency = [
		{ id: 1, value: 100, label: '100ms' },
		{ id: 2, value: 200, label: '200ms' },
		{ id: 4, value: 350, label: '350ms' },
		{ id: 5, value: 500, label: '500ms' },
		{ id: 6, value: 750, label: '750ms' }
	] as const;

	const endpointTabs = {
		BODY: { id: 1, label: 'Body' },
		HEADERS: { id: 2, label: 'Headers' },
		PATH_PARAMS: { id: 3, label: 'Path Params' },
		QUERY_PARAMS: { id: 4, label: 'Query Params' },
		SETTINGS: { id: 5, label: 'Settings' }
	} as const;

	let projects = $state(data?.projectsResponse);
	let collectionInitialState = data?.collections|| [];
	let collections = $state(normalizeUris(collectionInitialState, data?.projectsResponse[0].name));
	let lastEndpoint = collections[0]?.endpoints[collections[0].endpoints.length - 1] || {};
	let currentEndpointSelected = $state({
		...lastEndpoint,
		tab: endpointTabs.BODY.label,
		internalId: null,
		payload: lastEndpoint?.payload !== undefined
    ? (typeof lastEndpoint.payload === 'string' ? JSON.parse(lastEndpoint.payload) : lastEndpoint.payload)
    : {},
		headers: lastEndpoint?.headers?.length === 0 ? [{
				id: -1,
				key: '...',
				value: '...',
				isSelected: false
		}] : [...lastEndpoint?.headers ?? [], {key: '...',value: '...',isSelected: false}]
	});
	let element = $state<HTMLElement>();
	let isSaved = $state(true);
	let body = $derived(currentEndpointSelected?.payload);
	let formElement: HTMLFormElement | undefined= $state();
	let editorDoc = $derived(JSON.stringify(currentEndpointSelected?.payload, null, 2));
	let view: EditorView | undefined = $state();
	let isLogoVisible = $state(false);
	let inputs: HTMLInputElement[] | null = $state([]);

	/**
	 * Indicates if the current editor value (code) that is been written is
	 * parseable to a valid JSON format.
	 */
	let jsonBodyMessage: string = $state("");

	/**
	* Represents the current profile logged in.
	*/
	let profile:string = $state('dummy_user');

	/**
	* Flag that indicates if the current endpoint is been recorded.
	*/
	let isRecording: boolean = $state(false);

	const initialFormState = JSON.stringify(currentEndpointSelected?.payload|| '', null, 2);

	function createEditorView() {
		return new EditorView({
			parent: element,
			doc: editorDoc,
			extensions: [basicSetup, json()],
			dispatchTransactions: function (transactions, view) {
				view.update(transactions);
				transactions.forEach(function (transaction: Transaction) {
					if (transaction.docChanged) {
						const newObj = {
							...currentEndpointSelected,
							payload: transaction.state.doc.toString()
						};

						currentEndpointSelected = newObj;

						try {
							JSON.parse(transaction.state.doc.toString());
							jsonBodyMessage= "everything is ok!";
						} catch(err){
							jsonBodyMessage = err.message;
						}

						handleBodyChange();
					}
				});
			}
		});
	}

	onMount(function () {
		view = createEditorView();
	});

	async function handleAddEndpointClick() {
		const newEndpoint = {
			id: -1,
			internalId: generateUUID(),
			uri: '',
			verb: 'GET',
			payload: '{}',
			headers: [{
				id: -1,
				key: '...',
				value: '...',
				isSelected: false
			}],
			tab: endpointTabs.BODY.label,
			statusCode: 200,
			responseLatency: 200
		};
		currentEndpointSelected = newEndpoint;

		if (collections?.[0]?.endpoints?.length === 0) {
			collections[0].endpoints.push(newEndpoint);
			await tick();
			view = createEditorView();
		} else {
			view?.dispatch({
				changes: {
					from: 0,
					to: view.state.doc.length,
					insert: currentEndpointSelected.payload
				}
			});
			collections[0].endpoints.push(newEndpoint);
		}
	}

	function getFullUri(uri: string): string {
		const basePath = projects[0].name;
		return `${basePath}/${replaceLeadingSlash(uri)}`;
	}

	function replaceLeadingSlash(str: string): string {
		if(!str) {
			return "";
		}

		return str.replace("/", "");
	}

	function handleEndpointClick(endpoint: any) {
		currentEndpointSelected = {
			...endpoint,
			tab: endpointTabs.BODY.label,
			headers:currentEndpointSelected.headers
		};

		const payload = currentEndpointSelected?.payload || {};
		view.dispatch({
			changes: {
				from: 0,
				to: view.state.doc.length,
				insert: JSON.stringify(JSON.parse(payload), null, 2)
			}
		});
	}

	function getVerbColor(args: String) {
		if(!args) {
			return httpVerbs[0].color;
		}

		return httpVerbs.find((verb) => verb.label === args.toUpperCase().trim())?.color;
	}

	function handleVerbChange(event: any) {
		currentEndpointSelected.verb = event.target.value;

		const endpoint = collections[0].endpoints.find(
			(endpoint) => endpoint.id === currentEndpointSelected.id || endpoint.internalId === currentEndpointSelected.internalId
		);

		if(!endpoint) {
			throw new Error("Endpoint not found.");
		}

		endpoint.verb = event.target.value;
	}

	function onKeyDown({ key, ctrlKey, repeat }) {
		if (repeat) {
			return;
		}

		if (ctrlKey && key === 's') {
			event?.preventDefault();
			event?.stopPropagation(); // @@NOTE:: maintain this line to avoid duplicate requests to the server action.
			formElement.requestSubmit();
		}

		if (key === 'Escape') {
			event?.preventDefault();
			isLogoVisible = true;
			currentEndpointSelected.tab = "";
			return;
		}
	}

	async function handleSubmit(
		event: SubmitEvent & { currentTarget: EventTarget & HTMLFormElement }
	) {
		event.preventDefault();

		const data = new FormData(event.currentTarget, event.submitter);
		data.set('uri', data.get('uri')?.replace('/', ''));
		data.append('body', $state.snapshot(body));
		data.append('headers', JSON.stringify((currentEndpointSelected.headers)));

		/**
		 * @@NOTE:: When internalId is null means that the currentEndpoint is not
		 * artificialy created by the client, instead it is from the API response,
		 * so the HTTP call performed should be a PATCH indicating some change to the resource.
		 */
		if (!currentEndpointSelected.internalId) {
			await fetch("?/editEndpoint", {
				method: 'POST',
				body: JSON.stringify({fields: getDiffFields(), id: currentEndpointSelected.id})
			});
			isSaved = true;
			return;
		}

		 await fetch("?/addEndpoint", {
			method: 'POST',
			body: data
		});
		isSaved = true;
	}

	function handleUriChange(event: { target: { value: any; }; }) {
		let newUri = event.target.value;

		const endpoint = collections[0].endpoints.find(
			(endpoint) => endpoint.id === currentEndpointSelected.id
		);

		if (!endpoint) {
			console.warn('[WARN#CLIENT]: handleUriChange endpoint not found.');
			return;
		}

		newUri = newUri.startsWith("/") ? newUri : "/" + newUri;

		currentEndpointSelected.uri = newUri;
		endpoint.uri = newUri;
	}

	function handleBodyChange(): void {
		const diff = JSON.stringify(currentEndpointSelected.body) !== JSON.stringify(initialFormState);

		diff ? (isSaved = false) : (isSaved = true);
	}

	function isCurrentTab(tab: any) {
		return currentEndpointSelected.tab === tab.label;
	}

	async function handleTabChangeClick(tab: any) {
		isLogoVisible = false;
		const olderObj = { ...currentEndpointSelected };
		const newTab = { ...tab };
		const newObj = { ...currentEndpointSelected, tab: newTab.label };
		currentEndpointSelected = newObj;

		if (newObj.tab === endpointTabs.BODY.label && olderObj.tab === endpointTabs.BODY.label) {
			return;
		}

		if (newObj.tab === endpointTabs.BODY.label) {
			//editorDoc = JSON.parse(editorDoc);
			await tick();
			view = createEditorView();
		} else {
			view?.destroy();
		}
	}

	async function handleDblClickOnProjectArea(): Promise<void> {
		addInput()
		const newProjectAdded = { id: -1, name: '' };
		projects.push(newProjectAdded);
	}

	function isHeaderRowFilled(
		currIdx: number,
		header: { id: number; key: string; value: string; isSelected: boolean }
	): boolean {
		if (!header?.isSelected) {
			return true;
		}
		if (currentEndpointSelected?.headers?.length - 1 === currIdx) {
			return true;
		}
		return false;
	}

	function handleHeaderRowInputChange(
		currIdx: number,
		header: { id: number; key: string; value: string; isSelected: boolean }
	): void {
		header.isSelected = true;
		if (currentEndpointSelected.headers.length - 1 === currIdx) {
			currentEndpointSelected.headers.push({
				id: -1,
				key: '...',
				value: '...',
				isSelected: false
			});
		}
	}

	async function handleKeyDown(
		e: { key: string; currentTarget: { value: string; blur: () => void; }; }
	) {
		if (e.key === 'Enter' && e.currentTarget.value.trim() !== '') {
			e.currentTarget.blur();

			const data = { name: e.currentTarget.value.trim() };
			
			const res = await fetch('/api/projects', {
				method: 'POST',
				body: JSON.stringify(data)
			});

			projects[projects.length - 1].id = res.id;
		}
	}

	async function addInput() {
		inputs = [...inputs, null];

		await tick();

		const lastInput = inputs[inputs.length - 1];
        if (lastInput) {
            lastInput.focus();
        }
    }

	function handleChangeProfileClick(): void {
		console.log("handleChangeProfileClick clicked!");
	}

	async function handleStartStopRecordingClick(): Promise<void> {
		isRecording = !isRecording;

		const body = { uri: currentEndpointSelected.uri };

		await fetch('/api/start-stop/recording', {
			method: 'PATCH',
			body: JSON.stringify(body),
			headers: {
				"ldap": profile !== "credito" ? profile : ""
			}
		 });
	}

	function getRecordingIcon() {
		if (isRecording) {
			return "🔴";
		}
		return "▶";
	}

	// Source - https://stackoverflow.com/a/8809472
	// Posted by Briguy37, modified by community. See post 'Timeline' for change history
	// Retrieved 2026-01-06, License - CC BY-SA 4.0
	function generateUUID() { // Public Domain/MIT
		var d = new Date().getTime();//Timestamp
		var d2 = ((typeof performance !== 'undefined') && performance.now && (performance.now()*1000)) || 0;//Time in microseconds since page-load or 0 if unsupported
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			var r = Math.random() * 16;//random number between 0 and 16
			if(d > 0){//Use timestamp until depleted
				r = (d + r)%16 | 0;
				d = Math.floor(d/16);
			} else {//Use microseconds since page-load if supported
				r = (d2 + r)%16 | 0;
				d2 = Math.floor(d2/16);
			}
			return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
		});
	}

	function getDiffFields(): Array<Record<string, any>> {
		const collection = collectionInitialState.find(c =>c?.endpoints?.some(e => e.id === currentEndpointSelected.id));
		if (!collection) {
			throw new Error("Unable to find collection that belongs to the current selected endpoint.");
		}

		const initialEndpointState = collection.endpoints.find(e => e.id === currentEndpointSelected.id);
		if (!initialEndpointState) {
			throw new Error("Unable to find intial endpoint state that belongs to the current selected endpoint.");
		}

		const diffFields = [];
		for (const key of Object.keys(initialEndpointState)) {
			if (
				JSON.stringify(initialEndpointState[key]) !==
				JSON.stringify(currentEndpointSelected[key])
			) {
				if (key === "uri") {
					diffFields.push({
						key: key,
						value: currentEndpointSelected[key]
					});
				} else {
					diffFields.push({
						key: key,
						value: currentEndpointSelected[key]
					});
				}
			}
		}

		return diffFields;
	}

	/**
	 * @deprecated I am using this now only demo purposes, but on a production
	 * version should be considered a better approach.
	 */
	function normalizeUris(
		collectionInitialState: {
			id: number; uri: string; endpoints: any[];
		}[],
		name: any
	): any[] {
		if (collectionInitialState.length === 0) {
			return [];
		}

		const normalize = function(args: string): string {
			return args.replace("/"+name, "")
		}

		return collectionInitialState.map(c => ({
			...c,
			endpoints: c.endpoints.map(e => ({...e,uri: normalize(e.uri)}))
	  }));
	}
</script>

<svelte:window on:keydown={onKeyDown} />
<div>
	<header class="flex items-center justify-between border-b px-10">
		<div class="flex text-sm">
			<p class="cursor-pointer px-4 hover:bg-gray-100">Configurações</p>
			<p class="cursor-pointer px-4 hover:bg-gray-100">Ferramentas</p>
			<p class="cursor-pointer px-4 hover:bg-gray-100">Ajuda</p>
		</div>
		<div class="flex items-center gap-3">
			<button
				onclick={handleAddEndpointClick}
				class="cursor-pointer bg-gray-200 px-2 text-lg hover:bg-gray-300">
				+
			</button>
			<span class={`display-inline inline-block h-2 w-2 rounded-full ${isSaved ? 'bg-gray-300' : 'bg-green-500'}`}></span>
			<button onclick={handleStartStopRecordingClick} class="w-8 h-8 text-xs hover:border">{getRecordingIcon()}</button>
		</div>
	</header>

	<div class="flex h-screen">
		<div
			class="w-[20%] border-r select-none"
			ondblclick={handleDblClickOnProjectArea}
			role="button"
			tabindex="-1"
		>
			<span class="border-b ml-3">Projetos</span>

			{#each projects as project, idx}
				<div class="border-b cursor-pointer hover:bg-gray-100 w-full">
					<input 
						class="pt-1 pb-3 pl-3 w-full"
						value={project.name}
						name="project"
						onkeydown={handleKeyDown}
						bind:this={inputs[idx]}
					>
				</div>
			{/each}
		</div>
		<div class="w-[20%] border-r select-none">
			<span class="border-b ml-3">Coleções</span>
			{#each collections as collection}
				{#each collection.endpoints as endpoint}
					<button
						onclick={function () {
							handleEndpointClick(endpoint);
						}}
						class={`flex w-full cursor-pointer items-center
								justify-between border-b px-4 py-2
								text-left hover:bg-gray-100
								${currentEndpointSelected.id === endpoint.id && 'bg-gray-200'}
								`}
					>
						<div class="flex">
							<span>/</span>
							<p>{replaceLeadingSlash(endpoint.uri)}</p>
						</div>
						<span class={`${getVerbColor(endpoint.verb)} rounded-full border px-2 py-1 text-xs`}
							>{endpoint.verb || httpVerbs[0].label}</span
						>
					</button>
				{/each}
			{/each}
		</div>
		{#if collections[0]?.endpoints.length === 0}
			<div class="flex h-full w-full items-center justify-center">
				<img
					src={logo}
					alt="Mockaqui logo"
					width="350"
					height="350"
					class="opacity-20 grayscale"
				/>
			</div>
		{:else}
		<form bind:this={formElement} method="POST" class="w-[65%] border-r" onsubmit={handleSubmit}>
			<div>
				<div class="h-[30%]">
					<div class="flex gap-2 border-b">
						<select
							name="verb"
							class={`pl-3 ${getVerbColor(currentEndpointSelected.verb)} `}
							value={currentEndpointSelected.verb}
							onchange={function (event) {
								handleVerbChange(event);
							}}
						>
							{#each httpVerbs as verb}
								<option class={`pl-3 ${getVerbColor(verb.label)}`} value={verb.label}>
									{verb.label}
								</option>
							{/each}
						</select>
						<span>/</span>
						<div class="flex w-full items-center">
							<span class="cursor-default opacity-60">
								{currentEndpointSelected?.uri === "" ? projects[0].name + "/" : projects[0].name}
							</span>
							<input
								class="focus:ring-0 focus:outline-none"
								type="text"
								placeholder=""
								name="uri"
								bind:value={currentEndpointSelected.uri}
								oninput={handleUriChange}
							/>
						</div>
						<select name="statusCode" bind:value={currentEndpointSelected.statusCode}>
							{#each statusCode as status}
								<option class={`pl-3 ${status.color}`} value={status.code}>
									{status.code}
								</option>
							{/each}
						</select>
					</div>
				</div>
			</div>
			<div class="flex gap-5 border-b">
				{#each Object.entries(endpointTabs) as [_, v]}
					<button
						class={`cursor-pointer px-2 hover:bg-gray-200 ${isCurrentTab(v) && 'bg-gray-200'} `}
						onclick={function () {
							handleTabChangeClick(v);
						}}
						type="button"
					>
						<span>{v.label}</span>
					</button>
				{/each}
			</div>
			{#if !isLogoVisible}
				<div class="flex justify-between">
					<span class="pl-1 text-sm">{currentEndpointSelected.tab}</span>
					<select name="responseLatency" bind:value={currentEndpointSelected.responseLatency}>
						{#each responseLatency as latency}
							<option class={`pl-3`} value={latency.value}>
								~{latency.label}
							</option>
						{/each}
					</select>
				</div>
			{/if}
			<div class="h-[70%]" onkeydown={onKeyDown} role="none">
				{#if isLogoVisible === true}
					<div class="flex h-full w-full items-center justify-center">
						<img
							src={logo}
							alt="Mockaqui logo"
							width="350"
							height="350"
							class="opacity-20 grayscale"
						/>
					</div>
				{:else if currentEndpointSelected.tab === 'Headers'}
					<div>
						<table class="w-full border">
							<thead class="border">
								<tr class="border">
									<th class="border">&nbsp;</th>
									<th class="border">Key</th>
									<th class="border">Value</th>
								</tr>
							</thead>
							<tbody>
								{#each currentEndpointSelected?.headers as header, idx}
									<tr class="border">
										<td
											class={`border ${isHeaderRowFilled(idx, header) && 'border-gray-300' } px-3`}
										>
											<input
												type="checkbox"
												bind:checked={header.isSelected}
												name="isHeaderSelected"
											/>
										</td>
										<td
											class={`border ${isHeaderRowFilled(idx, header) && 'border-gray-300' } px-3`}
										>
											<input
												class={`outline-none ${isHeaderRowFilled(idx, header) && 'text-gray-300' }`}
												type="text"
												name="headerKey"
												bind:value={header.key}
												oninput={function () {
													handleHeaderRowInputChange(idx, header);
												}}
											/>
										</td>
										<td
											class={`border ${isHeaderRowFilled(idx, header) && 'border-gray-300'} px-3`}
										>
											<input
												class={`outline-none ${isHeaderRowFilled(idx, header) && 'text-gray-300'}`}
												type="text"
												name="headerValue"
												bind:value={header.value}
												oninput={function () {
													handleHeaderRowInputChange(idx, header);
												}}
											/>
										</td>
									</tr>
								{/each}
							</tbody>
						</table>
					</div>
				{:else}
					<div class="max-h-[80vh] cursor-auto overflow-y-auto" bind:this={element}></div>
					<p>{jsonBodyMessage}</p>
					<button onclick={handleChangeProfileClick} class="flex gap-1 absolute bottom-5 right-5 cursor-pointer select-none hover:text-gray-600">
						Current profile: <u>{profile}</u> 🗘
					</button>
				{/if}
			</div>
		</form>
		{/if}
	</div>
</div>

<style>
	@import url('https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap');

	* {
		font-family: 'Inter', sans-serif;
		font-optical-sizing: auto;
		font-weight: 300;
		font-style: normal;
	}
</style>
