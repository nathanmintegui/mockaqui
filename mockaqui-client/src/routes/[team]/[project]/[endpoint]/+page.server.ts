import type { Actions } from './$types';

type HeadersType = {
	id: number,
	key: string,
	value: string,
	isSelected: boolean
}

function parseHeaders(headers: FormDataEntryValue) {
	if (headers.toString() === '{}') {
		return [];
	}

	const headersObj = JSON.parse(headers.toString()) as Array<HeadersType>;

	return headersObj
		.slice(0, -1)
		.map(header => {
			return {
				key: header.key,
				value: header.value,
				isSelected: header.isSelected
			}
		});
};

export const actions = {
	addEndpoint: async ({ request }) => {
		const data = await request.formData();

		const verb = data.get('verb');
		const uri = "/" + data.get('uri');
		const body = data.get('body')!;
		const statusCode = data.get('statusCode');
		const headers = data.get('headers');
		const responseLatency = data.get('responseLatency');

		const req = {
			verb,
			uri,
			statusCode,
			payload: JSON.stringify(JSON.parse(body)),
			headers: parseHeaders(headers),
			responseLatency
		}

		const serviceId = 1;
		const collectionId = 1;

		await fetch(
			`http://localhost:8080/api/services/${serviceId}/collections/${collectionId}/endpoints`,
			{
				method: 'POST',
				body: JSON.stringify(req),
				headers: {
					"Content-type": "application/json"
				}
			}
		);
	},
	editEndpoint: async function({ request }) {
		const data: Array<Record<string, any>> = await request.json()

		let body = {
			uri: null,
			verb: null,
			statusCode: null,
			headers: null,
			payload: null,
			responseLatency: null
		};

		// @@Refactor :: Find a cleaner way to handle this...
		data.fields.forEach(function(item) {
			if (item.key === 'uri') {
				body = { ...body, uri: item.value }
			}
			if (item.key === 'method') {
				body = { ...body, verb: item.value }
			}
			if (item.key === 'statusCode') {
				body = { ...body, statusCode: item.value }
			}
			if (item.key === 'headers') {
				body = {
					...body,
					headers: item.value.slice(0, -1)
						.map(header => {
							return {
								key: header.key,
								value: header.value,
								isSelected: header.isSelected
							}
						})
				}
			}
			if (item.key === 'body') {
				body = { ...body, payload: JSON.parse(item.value) }
			}
			if (item.key === 'responseLatency') {
				body = { ...body, responseLatency: item.value }
			}
		});

		const id = data.id;
		if (!id) {
			throw new Error("[ERROR] :: [editEndpoint]: Unable to get a valid id.");
		}

		// @@NOTE :: Filter only the non null values to send through PATCH
		// request to the mock api server.
		const diffFields = Object.fromEntries(
			Object.entries(body).filter(([key, value]) => value !== null)
		);

		console.log('diffFields', diffFields);

		try {
			const res = await fetch(
				`http://localhost:8080/api/endpoints/${id}`,
				{
					method: 'PATCH',
					body: JSON.stringify(diffFields),
					headers: {
						"Content-type": "application/json"
					}
				}
			);
			if (!res.ok) {
				console.error("[ERROR] :: [editEndpoint]: unable to send patch request to mock api.");
				return;
			}
			console.log("[INFO]  :: [editEndpoint]: Request sent succesfully.");
		} catch (err) {
			console.error("[ERROR] :: [editEndpoint]: reason ", err);
		}
	}
} satisfies Actions;
